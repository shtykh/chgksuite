package shtykh.quedit.numerator;

import shtykh.quedit.question.Question;

/**
 * Created by shtykh on 05/10/15.
 */
public class QuestionNaturalNumerator extends NaturalNumerator<Question> {
	public QuestionNaturalNumerator() {
		this(1);
	}

	public QuestionNaturalNumerator(int first, String... zeroNumbers) {
		super(first, zeroNumbers);
	}
}
