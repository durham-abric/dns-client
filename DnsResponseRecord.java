public class DnsResponseRecord{

    private int sec_life, rdata_len, preference;
    private String name;
    private boolean authority;
    private QuestionType type;

    public int getSecLfe()
    {
		return this.sec_life;
	}

    public void setSecLife(int sec_life)
    {
		this.sec_life = sec_life;
	}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuthority() {
        return this.authority;
    }

    public void setAuthority(boolean authority) {
        this.authority = authority;
    }

    public QuestionType getType() {
        return this.type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

}