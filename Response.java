import java.net.*;
import java.nio.ByteBuffer;

import sun.awt.RepaintArea;

public class Response{
    
    private byte[] raw_response, response_id;
    private boolean[] header_codes; //[QR, AA, TC, RD, RA]
    private int RCODE, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    private QuestionType type;

    private DnsResponseRecord[] answers;
    private DnsResponseRecord[] additional;


    public Response(byte[] raw_response, int req_size, byte[] expected_id, QuestionType expected_type){
        this.raw_response = raw_response;
        this.type = type;
        header_codes = new boolean[5];
    }

    private void parseResponseHeader(){
        response_id = new byte[2];
        response_id[0] = raw_response[0];
        response_id[1] = raw_response[1];
        validateID();

        //Get QR value & validate packet is response (not request)
        header_codes[0] = ((raw_response[2] >> 7) & 1) == 1;
        if(!header_codes[0]) throw new InvalidActivityException("Error parsing response packet - QR = 0 implies request not response.");
    
        //Get AA value
        header_codes[1] = ((raw_response[2] >> 2) & 0x01) == 1;

        //Get TC value
        header_codes[2] = ((raw_response[2] >> 1) & 0x01) == 1;

        //Get RD value
        header_codes[3] = ((raw_response[2]) & 0x01) == 1;

        //Get RA value
        header_codes[4] = ((raw_response[3] >> 7) & 0x01) == 1;

        //Get RCODE with bitmask 
        RCODE = raw_response[3] & 0x0F;

        //Get QDCOUNT by converting byte[2] to 16-bit (short) int
        byte[] temp_qdcount = {raw_response[4], raw_response[5]};
        ByteBuffer temp_buffer = ByteBuffer.wrap(temp_qdcount);
        QDCOUNT = temp_buffer.getShort();

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
 
    private validateID(byte[] expected){
        if(response_id[0] != expected[0] || response_id[1] != expected[1]) throw new Exception("ID of response does not match request."); 
    }

    private validateType(QuestionType expected){
        if(type != expected) throw new Exception("ID of response does not match request."); 
    }

}