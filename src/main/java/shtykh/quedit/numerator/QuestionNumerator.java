package shtykh.quedit.numerator;

import shtykh.quedit.question.Question;

/**
 * Created by shtykh on 05/10/15.
 */
public interface QuestionNumerator extends Numerator {
	default void renumber(Question question) {
			question.setNumber(getNumber(question.index()));
	}
}
