public class Main {
    public static void main(String args[]) throws Exception {
        try {
            DnsClient client = new DnsClient(args);
            client.sendDnsRequest();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}