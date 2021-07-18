package sequencingConstraints;

import java.util.LinkedHashSet;
import java.util.Set;

import util.Constants;

/**
 * sequencing constraint format
 * 
 * @author Feng Duan
 *
 */
public class Constraint {
	
	String operator;
	Constraint leftOperand;
	Constraint rightOperand;

	public Constraint(String op, Constraint left, Constraint right) {
		operator = op;
		leftOperand = left;
		rightOperand = right;
	}
	
	public boolean isNested(){
		Set<String> sequencingOperators = new LinkedHashSet<String>();
		for (String op : Constants.sequencingOperators)
			sequencingOperators.add(op);
		
		Set<String> commonOperators = new LinkedHashSet<String>();
		for (String op : Constants.commonOperators)
			commonOperators.add(op);
		
		// If the expression's operator is in sequencingOperators, 
		// and its leftOperand's operator is in either sequencingOperators or commonOperators,
		// then it is nested
		if (sequencingOperators.contains(operator) && leftOperand != null
			&& (sequencingOperators.contains(leftOperand.operator) || commonOperators.contains(leftOperand.operator)))
			return true;
		
		return false;
	}
	
	// Check if a constraint is a single event "e1" as Constraint("e1", null, null),
	// or an event set as Constraint("{", Constraint of "e1,e2,...", null).
	// Notice that the format of constraint is Constraint(operand, leftOperand, rightOperand).
	public boolean isEventOrSet(){
		// If the expression's leftOperand and rightOperand are null, then it is a single event.
		if (leftOperand == null && rightOperand == null)
			return true;
		
		// If the expression's operator is OPENBRACE, then it is an event set.
		if (operator.equals(Constants.OPENBRACE))
			return true;
		
		return false;
	}
	
	/* 
	 * call left search for print in plain
	 */
	public String plainText(){
		return search(this);
	}
	
	public String search(Constraint cons){
		if (cons == null)
			return "";
		
		if (cons.leftOperand == null && cons.rightOperand == null)
			return cons.operator; // since now the cons is an event expression
		
		String op = cons.operator;
		String left = search(cons.leftOperand);
		String right = search(cons.rightOperand);
		
		String prefixText = op + "(" + left + "," + right + ")";
		
		return prefixText;
	}


	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}


	/**
	 * @return the leftOperand
	 */
	public Constraint getLeftOperand() {
		return leftOperand;
	}


	/**
	 * @return the rightOperand
	 */
	public Constraint getRightOperand() {
		return rightOperand;
	}
}
