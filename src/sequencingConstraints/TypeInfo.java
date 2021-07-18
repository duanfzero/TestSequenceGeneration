package sequencingConstraints;

/*
 * This class is used for type checking and store AST in constraint parser.
 */
public class TypeInfo {
	int type;
	private String text;
	
	// constraint in AST form of no more than two operands
	private Constraint constraint;
	
	public void setType (int type) {
		this.type = type;
	}
	public int getType () {
		return type;
	}
	
	public void setText (String text) {
		this.text = text;
	}
	public String getText () {
		return text;
	}
	
	// store constraint info in parsing order as concrete syntax tree
	public void setConstraint (Constraint cons) {
		constraint = cons;
	}
	public Constraint getConstraint () {
		return constraint;
	}
}
