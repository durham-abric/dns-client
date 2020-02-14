public class DnsResponseRecord{

    private int sec_life, num_bytes;
    private short rdata_len, preference;
    private String name, rdata;
    private boolean authority;
    private QuestionType type;

    public DnsResponseRecord(boolean authority){
        this.authority = authority;
    }

    public short getRdataLen() {
        return this.rdata_len;
    }

    public void setRdataLen(short rdata_len) {
        this.rdata_len = rdata_len;
    }

    public short getPreference() {
        return this.preference;
    }

    public void setPreference(short preference){
        this.preference = preference;
    }

    public int getSecLfe()
    {
		return this.sec_life;
	}

    public void setSecLife(int sec_life)
    {
		this.sec_life = sec_life;
    }
    
    public int getNumBytes()
    {
		return this.num_bytes;
	}

    public void setNumBytes(int num_bytes)
    {
		this.num_bytes = num_bytes;
	}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRdata() {
        return this.rdata;
    }

    public void setRdata(String rdata) {
        this.rdata = rdata;
    }

    public boolean isAuthority() {
        return this.authority;
    }

    public void setAuthority(boolean authority) {
        this.authority = authority;
    }

    private String getAuthorityStr(){
        return authority ? "auth" : "nonauth";
    }

    public QuestionType getType() {
        return this.type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public void outputToClient(){
        switch(type){
            case A:
                System.out.println(String.format("IP\t%s\t%d\t%s", rdata, sec_life, getAuthorityStr()));
                break;
            case NS:
                System.out.println(String.format("NS\t%s\t%d\t%s", rdata, sec_life, getAuthorityStr()));
                break;
            case MX:
                System.out.println(String.format("MX\t%s\t%d\t%d\t%s", rdata, preference, sec_life, getAuthorityStr()));
                break;
            case CNAME:
                System.out.println(String.format("CNAME\t%s\t%d\t%s", rdata, sec_life, getAuthorityStr()));
                break;
            case UNRECOGNIZED:
                System.out.println(String.format("...\t%s\t%d\t%s", rdata, sec_life, getAuthorityStr()));
                break;
        }
    }

}