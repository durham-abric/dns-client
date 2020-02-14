import java.net.*;
import java.nio.ByteBuffer;

public class Response{
    
    private byte[] raw_response, response_id;
    private boolean[] header_codes; //[QR, AA, TC, RD, RA]
    private int RCODE, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    private QuestionType type;

    private DnsResponseRecord[] answers;
    private DnsResponseRecord[] additional;

    public Response(byte[] raw_response, int req_size, byte[] expected_id, QuestionType expected_type){
        this.raw_response = raw_response;
        header_codes = new boolean[5];

        parseResponseHeader(expected_id, expected_type);

        answers = new DnsResponseRecord[ANCOUNT];
        additional = new DnsResponseRecord[ARCOUNT];

        parseAnswers(req_size);
    }

    private void parseResponseHeader(byte[] expected_id, QuestionType expected_type){
        response_id = new byte[2];
        response_id[0] = raw_response[0];
        response_id[1] = raw_response[1];
        validateID(expected_id);
        //validateType(expected_type);

        //Get QR value & validate packet is response (not request)
        header_codes[0] = ((raw_response[2] >> 7) & 1) == 1;
        if(!header_codes[0]) throw new RuntimeException("Error parsing response packet - QR = 0 implies request not response.");

        //Get AA value
        header_codes[1] = ((raw_response[2] >> 2) & 0x01) == 1;

        //Get TC value
        header_codes[2] = ((raw_response[2] >> 1) & 0x01) == 1;

        //Get RD value
        header_codes[3] = ((raw_response[2]) & 0x01) == 1;

        //Get RA value
        header_codes[4] = ((raw_response[3] >> 7) & 0x01) == 1;

        //Get RCODE with bitmask & validate error code
        RCODE = raw_response[3] & 0x0F;
        switch(RCODE){
            case 1:
                throw new RuntimeException("Format error: the name server was unable to interpret the query.");
            case 2: 
                throw new RuntimeException("Server failure: the name server was unable to process this query due to a problem with the name server.");
            case 3:
                throw new RuntimeException("Name error: meaningful only for responses from an authoritative name server, this code signifies that the domain name referenced in the query does not exist.");
            case 4:
                throw new RuntimeException("Not implemented: the name server does not support the requested kind of query.");
            case 5:
                throw new RuntimeException("Refused: the name server refuses to perform the requested operation for policy reasons.");
            default:
                break;
        }

        //Get QDCOUNT by converting byte[2] to 16-bit (short) int
        byte[] temp_qdcount = {raw_response[4], raw_response[5]};
        ByteBuffer temp_buffer = ByteBuffer.wrap(temp_qdcount);
        QDCOUNT = temp_buffer.getShort();
        if(QDCOUNT != 1) System.out.println(String.format("Incorrect Response State: %d Questions responded to (1 expected)."));

        //Get ANCOUNT by converting byte[2] to 16-bit (short) int
        byte[] temp_ancount = {raw_response[6], raw_response[7]};
        temp_buffer = ByteBuffer.wrap(temp_ancount);
        ANCOUNT = temp_buffer.getShort();

        //Get NSCOUNT by converting byte[2] to 16-bit (short) int
        byte[] temp_nscount = {raw_response[8], raw_response[9]};
        temp_buffer = ByteBuffer.wrap(temp_nscount);
        NSCOUNT = temp_buffer.getShort();

        //Get ARCOUNT by converting byte[2] to 16-bit (short) int
        byte[] temp_arcount = {raw_response[10], raw_response[11]};
        temp_buffer = ByteBuffer.wrap(temp_arcount);
        ARCOUNT = temp_buffer.getShort();
    }
 
    private void validateID(byte[] expected){
        if(response_id[0] != expected[0] || response_id[1] != expected[1]) throw new RuntimeException("ID of response does not match request."); 
    }

    private void validateType(QuestionType expected){
        if(type != expected) throw new RuntimeException("Query type of response does not match request."); 
    }

    private void parseAnswers(int req_size){
        int answer_start = req_size;
        for(int answer_num = 0; answer_num < ANCOUNT; answer_num++){
            answers[answer_num] = parseAnswer(answer_start);
            answer_start += answers[answer_num].getNumBytes();
        }

        //No need to save NS responses (but must calculate bytes used by them)
        for(int ns_num = 0; ns_num < NSCOUNT; ns_num++){
            answer_start += parseAnswer(answer_start).getNumBytes();
        }

        for(int additional_num = 0; additional_num < ARCOUNT; additional_num++){
            additional[additional_num] = parseAnswer(answer_start);
            answer_start += additional[additional_num].getNumBytes();
        }
    }

    private DnsResponseRecord parseAnswer(int answer_index){
        DnsResponseRecord record = new DnsResponseRecord(header_codes[1]);

        //Parse NAME (variable size)
        int answer_offset = answer_index;
        AnswerData answer_name = parseName(answer_index);
        record.setName(answer_name.getName());
        answer_offset += answer_name.getBytes();

        //Parse TYPE
        byte[] type = new byte[2];
        type[0] = raw_response[answer_offset];
        type[1] = raw_response[answer_offset + 1];
        if(type[0] != 0x00) throw new RuntimeException("Incorrect response type code received.");
        switch(type[1]){
            case 0x01:
                record.setType(QuestionType.A);
                break;
            case 0x02:
                record.setType(QuestionType.NS);
                break;
            case 0x0f:
                record.setType(QuestionType.MX);
                break;
            case 0x05:
                record.setType(QuestionType.CNAME);
                break;
            default:
                throw new RuntimeException("Incorrect response type code received.");
        }
        answer_offset += 2;

        //Parse CLASS
        byte[] answer_class = {raw_response[answer_offset], raw_response[answer_offset + 1]};
        if((int) answer_class[0] != 0 || (int) answer_class[1] != 1) throw new RuntimeException("Incorrect response class code received.");
        answer_offset += 2;

        //Parse TTL
        byte[] ttl = {raw_response[answer_offset], raw_response[answer_offset + 1], raw_response[answer_offset + 2], raw_response[answer_offset + 3]};
        ByteBuffer temp_wrapper = ByteBuffer.wrap(ttl);
        record.setSecLife(temp_wrapper.getInt());
        answer_offset += 4;

        //Parse RDLENGTH
        byte[] rd_length = {raw_response[answer_offset], raw_response[answer_offset + 1]};
        temp_wrapper = ByteBuffer.wrap(rd_length);
        record.setRdataLen(temp_wrapper.getShort());
        answer_offset += 2;

        //Parse RDATA
        String rdata = "";
        switch(record.getType()){
            case A:
                try{
                    byte[] answer_ip = {raw_response[answer_offset], raw_response[answer_offset + 1], raw_response[answer_offset + 2], raw_response[answer_offset + 3]};
                    InetAddress address = InetAddress.getByAddress(answer_ip);
                    rdata = address.toString();
                }catch(UnknownHostException uhe){
                    System.out.println("IP address could not be resolved.");
                }
                break;
            case NS:
                rdata = parseName(answer_offset).getName();
                break;
            case MX:
                byte[] preference = {raw_response[answer_offset], raw_response[answer_offset + 1]};
                temp_wrapper = ByteBuffer.wrap(preference);
                record.setPreference(temp_wrapper.getShort());
                answer_offset += 2;
                rdata = parseName(answer_offset).getName();
            case CNAME:
                AnswerData cname_raw = parseName(answer_offset);
                rdata = cname_raw.getName();
                break;
        }

        record.setRdata(rdata);
        record.setNumBytes(answer_offset + record.getRdataLen() - answer_index);
        return record;
    }

    private AnswerData parseName(int answer_index){
        int size = raw_response[answer_index];
        int byte_count = 0;
        String NAME = "";
        boolean name_begun = true;

        //Size = 0 implies end of NAME
        while(size > 0){
            if(!name_begun) NAME += ".";
            //Byte is int (# following char bytes)
            if((size & 0xC0) == 192){
                byte[] offset_size = {((byte) (raw_response[answer_index] & 0x3F)), raw_response[answer_index + 1]};
                ByteBuffer temp_wrapper = ByteBuffer.wrap(offset_size);
                NAME += parseName(temp_wrapper.getShort());
                answer_index += 2; byte_count += 2; size = 0;
            }
            //Byte can be converted into 'size' consecutive chars (String)
            else{
                NAME += buildStringFromBytes(answer_index);
                answer_index += size + 1; byte_count += size + 1; size = raw_response[answer_index];
            }
            name_begun = false;
        }

        return new AnswerData(NAME, byte_count);
    }

    private String buildStringFromBytes(int byte_num){
        String str = "";
        int size = raw_response[byte_num];
        for(int index = 0; index < size; index++){str += (char) raw_response[byte_num + index + 1];}
        return str;
    }

    public void outputToClient(){
        System.out.println(String.format("\n***Answer Section (%d records)***", ANCOUNT));
        for(int i = 0; i < ANCOUNT; i++){
            answers[i].outputToClient();
        }

        System.out.println(String.format("\n***Additional Section (%d records)***", ARCOUNT));
        for(int j = 0; j < ARCOUNT; j++){
            additional[j].outputToClient();
        }
    }

}