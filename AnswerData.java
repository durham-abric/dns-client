//POJO to bundle the server name in answer & size of name
public class AnswerData{
    private String name;
    private int bytes;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBytes() {
        return this.bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public AnswerData(String name, int bytes){
        this.name = name;
        this.bytes = bytes;
    }

}