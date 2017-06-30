
public class AsciiProg {

	public String content, name, password;
	
	public AsciiProg(String content, String name, String password) {
		this.content = content;
		this.name = name;
		this.password = password;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name + "\nPassword: " + (this.password.isEmpty() ? "<no password>" : this.password) + this.content;
	}
}
