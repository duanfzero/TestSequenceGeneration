package util;

/**
 * this class is used to define the type constants used in the application
 * 
 * @author Feng Duan
 *
 */
public class Constants {
	
	// Set a special type for event/events in sequencing constraint parser
	public static final int TYPE_EVENT = 3;
    
	// the following constants are used in constraint parser
//    public static final int TYPE_INVALID = -1;
//    public static final int TYPE_INT = 0;
//    public static final int TYPE_STRING = 1;
    public static final int TYPE_BOOL = 2;
    
	// Sequencing Operators for both checking nested and solving expression
    public static final String ALWAYS = "_";
    public static final String IL = "+-";	// for the latest notation format in my dissertation, it should be "+-"
    public static final String IR = "-+";
    public static final String IN = "~";
    public static final String GL = "+...";
    public static final String GR = "...+";
    public static final String GN = ".~.";
    public static final String[] sequencingOperators = 
    								{IR, IL, IN, GR, GL, GN, ALWAYS};
    
    // Common Operators on constraint can be mapping to the common operations on automata
    // i.e., NOT "!" = the Complement of the automaton equivalent to constraint,
    // CONCAT "." = the Concatenation of two automata, AND "&&" = Intersection, OR "||" = Union.
    public static final String OR = "||";
    public static final String AND = "&&";
    public static final String CONCAT = ".";
    public static final String NOT = "!";
    public static final String[] commonOperators = {OR, AND, CONCAT, NOT};
    
    // Event Set's Left Brace as its operator
    public static final String OPENBRACE = "{";
	
}
