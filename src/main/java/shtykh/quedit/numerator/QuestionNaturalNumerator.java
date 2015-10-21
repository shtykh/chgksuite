package shtykh.quedit.numerator;

/**
 * Created by shtykh on 05/10/15.
 */
public class QuestionNaturalNumerator extends NaturalNumerator implements QuestionNumerator {
	public QuestionNaturalNumerator() {
		this(1);
	}

	public QuestionNaturalNumerator(int first, String... zeroNumbers) {
		super(first, zeroNumbers);
	}
}
