import java.io.IOException;
import java.io.IOError;
import java.net.*;
import java.util.*;

public class DnsClient{

    
    public static final int MAX_PACKET_SIZE = 512;
    private QuestionType type = QuestionType.A;
    private int timeout_sec = 30;
    private int retry_attempts = 1;
    private byte[] server_ip = new byte[4];
    private String server_address, site_name;
    private int port = 53;

    public DnsClient(String args[]){
        try{
            parseInput(args);
            if(server_address == null || site_name == null || server_address.isEmpty() || site_name.isEmpty()) throw new IllegalArgumentException("Invalid calling syntax - Server IP address or site name missing.");
        }catch (Exception e){
            System.out.println(e);
            throw new IllegalArgumentException("Invalid calling syntax - check arguments & retry.");
        }

        System.out.println("DnsClient sending request for " + site_name);
        System.out.println("Server: " + server_address);
        System.out.println("Request type: " + type.name());
    }

    private void validateIP(String address){
        String[] ip_components = address.split(".");
        if(ip_components.length != 4){
            throw new IllegalArgumentException("IP address entered (" + address + ") is invalid - IP must have 4 components.");
        }

         for(int i = 0; i < 4; i++){
            Integer component = Integer.parseInt(ip_components[i]);
            if(component < 0 || component > 255) throw new IllegalArgumentException("IP address entered (" + address + ") is invalid - components must be in range [0, 255].");
            server_ip[i] = component.byteValue();
         }
    }

    private void parseInput(String args[]){
        List<String> arg_list = Arrays.asList(args);
        Iterator arg_iterator = arg_list.iterator();

        while(arg_iterator.hasNext()){
            String current_arg = arg_iterator.next();
            switch(current_arg){
                case "-t":
                    timeout_sec = Integer.parseInt((String)arg_iterator.next());
                    if(timeout_sec < 1) throw new IllegalArgumentException("Timeout Error: -t <timeout> must have a positive value.");
                    break;
                case "-r":
                    retry_attempts = Integer.parseInt((String)arg_iterator.next());
                    if(retry_attempts < 0) throw new IllegalArgumentException("Retry Error: -r <retry> must have a positive value.");
                    break;
                case "-p":
                    port = Integer.parseInt((String)arg_iterator.next());
                    if(port < 1 || port > 65535) throw new IllegalArgumentException("Port Error: -p <port> must have a value in [1, 65535].");
                    break;
                case "-mx":
                    if(type == QuestionType.A){type = QuestionType.MX;}
                    else{throw new IllegalArgumentException("Query type already set to " + type.name());}
                    break;
                case "-ns":
                    if(type == QuestionType.A){type = QuestionType.NS;}
                    else{throw new IllegalArgumentException("Query type already set to " + type.name());}
                    break;
                default:
                    if (current_arg.charAt(0) != "@"){
                        throw new IllegalArgumentException("The server IP must follow the form: @#.#.#.#");
                    }else{
                        server_address = current_arg.substring(1);
                        validateIP(server_address);
                        site_name = arg_iterator.next();
                    }

            }
        }
    }





}