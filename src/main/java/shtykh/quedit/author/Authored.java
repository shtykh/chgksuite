package shtykh.quedit.author;

/**
 * Created by shtykh on 01/10/15.
 */
public interface Authored {
	public Person getAuthor();
	
	public void setAuthor(MultiPerson author);
	
	public default void setAuthors(SinglePerson... authors) {
		MultiPerson multiAuthor = new MultiPerson();
		for (SinglePerson author : authors) {
			multiAuthor.add(author);
		}
		setAuthor(multiAuthor);
	}
}
