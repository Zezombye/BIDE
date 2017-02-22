
public class AsciiProg {

	public String content, name, password;
	public int lineStart;
	
	public AsciiProg(String content, String name, String password, int lineStart) {
		this.content = content;
		this.name = name;
		this.password = password;
		this.lineStart = lineStart;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name + "\nPassword: " + (this.password.isEmpty() ? "<no password>" : this.password) + "\nStarts at line: " + this.lineStart + "\n" + this.content;
	}
}
