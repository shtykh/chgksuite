package shtykh.quedit._4s;

/**
 * Created by shtykh on 05/10/15.
 */
public interface _4Sable {
	String to4s();
	
	default void append(StringBuilder sb, FormParameterMaterial4s material4s) {
		sb.append(material4s.to4s());
	}
}
