import java.nio.ByteBuffer;
import java.util.*;

import com.oracle.jrockit.jfr.InvalidValueException;

public class Request{

    private String site_name;
    private QuestionType type;
    private byte[] request_id;
    
    //Static -> Multiple requests will share pseudo-random generator
    private static Random id_generator = new Random();

    public Request(String name, QuestionType t){
        // Name and type set in DnsClient call
        site_name = name;
        type = t;
        // Generate new ID for this request (randomized...)
        request_id = new byte[2];
        id_generator.nextBytes(request_id);
    }

    public byte[] getRequestPacket(){
        //12 Bytes for header, 4 bytes for question header codes, 1 byte for termination byte (0) of QNAME, variable bytes for QNAME
        ByteBuffer packet = ByteBuffer.allocate(17 + calculateNameLength());
        buildHeader(packet);
        buildQuestion(packet);
        return packet.array();
    }

    private void buildHeader(ByteBuffer b){
        //First 2 bytes - ID
        b.put(request_id);
        //3rd Byte - QR + Opcode + AA + TC + RD
        b.put((byte)0x01);
        //4th Byte - RA + Z + RCODE
        b.put((byte)0x00);
        //5th & 6th Bytes - QDCOUNT
        b.put((byte)0x00); b.put((byte)0x01);
        //7th & 8th Bytes - ANCOUNT
        b.put((byte)0x00); b.put((byte)0x00);
        //9th & 10th Bytes -NSCOUNT
        b.put((byte)0x00); b.put((byte)0x00);
        //11th & 12th Bytes - ARCOUNT
        b.put((byte)0x00); b.put((byte)0x00);
    }
    
    private void buildQuestion(ByteBuffer b){
        String[] name_components = site_name.split(".");
        String component;
        int temp;

        //Add QNAME to buffer
        for(int component_num = 0; component_num < name_components.length; component_num++){
            //Add byte for length of component
            component = name_components[component_num];
            temp = component.length();
            b.put((byte)temp);
            for(int character_num = 0; character_num < component.length(); character_num++){
                //Add individual characters of component (as bytes)
                temp = (int) component.charAt(character_num);
                b.put((byte)temp);
            }
        }
        //Add termination byte
        b.put((byte)0x00);

        //Add bytes for QCLASS (dependent on QuestionType)
        switch(type){
            case A:
                b.put((byte)0x0001);
                break;
            case MX:
                b.put((byte)0x000f);
                break;
            case NS:
                b.put((byte)0x0002);
                break;
        }

        //Add bytes for QCLASS
        b.put((byte)0x0001);
    }

    private int calculateNameLength(){
        String[] components = site_name.split(".");
        int len = components.length;
        for(int i = 0; i < components.length; i ++){
            len += components[i].length();
        }
        return len;
    }

    public byte[] getID(){
        return request_id;
    }

    public QuestionType getType(){
        return type;
    }

}