
public class AsciiProg {

	public String content, name, option;
	public int type;
	
	public AsciiProg(String content, String name, String option, int type) {
		this.content = content;
		this.name = name;
		this.option = option;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name + "\nPassword: " + (this.option.isEmpty() ? "<no password>" : this.option) + this.content;
	}
}
