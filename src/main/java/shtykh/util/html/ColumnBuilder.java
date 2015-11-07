package shtykh.util.html;

/**
 * Created by shtykh on 07/11/15.
 */
public abstract class ColumnBuilder<T extends TableRowMaterial> {
	public abstract String getCell(T source);
}
