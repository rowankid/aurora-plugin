package aurora.plugin.entity.gen;

import java.util.ArrayList;

import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.DataType;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.entity.model.Record;

public class SqlGenerator implements IEntityConst {
	private static final String line_sep = String.format("%n");
	private static final String prefix = "    ";
	private static final String header = "create table %s (" + line_sep;
	private static final String tail = ")";
	private static final String column_model = prefix + "%%-%ds %%s";
	private static final String comment_model = "comment on column %%-%ss is '%%s'";

	private BMModel model;
	private String name;
	private int maxNameLength = 0;

	public SqlGenerator(BMModel model, String name) {
		this.model = model;
		this.name = name;
	}

	public String[] gen() {
		ArrayList<String> sqls = new ArrayList<String>();
		Record[] rs = model.getRecords();
		StringBuilder sb = new StringBuilder(10000);
		sb.append(String.format(header, name));
		maxNameLength = getMaxNameLength();
		String cm = String.format(column_model, maxNameLength);
		String t = (rs.length == 0) ? "" : ",";
		Record r = model.getPkRecord();
		sb.append(String.format(cm, r.getName(), getSqlType(r.getType()))
				+ " not null" + t + line_sep);
		for (int i = 0; i < rs.length; i++) {
			t = (i == rs.length - 1) ? "" : ",";
			sb.append(String.format(cm, rs[i].getName(),
					getSqlType(rs[i].getType()))
					+ t + line_sep);
		}
		sb.append(tail);
		sqls.add(sb.toString());
		addComment(sqls);
		String[] sqlArr = new String[sqls.size()];
		sqls.toArray(sqlArr);
		return sqlArr;
	}

	private void addComment(ArrayList<String> sqls) {
		String cm = String.format(comment_model, name.length() + 1
				+ maxNameLength);
		Record pkr = model.getPkRecord();
		sqls.add(String.format(cm, name + "." + pkr.getName(), pkr.getPrompt()));
		for (Record r : model.getRecordList()) {
			sqls.add(String.format(cm, name + "." + r.getName(), r.getPrompt()));
		}
	}

	private int getMaxNameLength() {
		int length = 0;
		for (Record r : model.getRecordList()) {
			int l = r.getName().length();
			if (l > length)
				length = l;
		}
		int l = model.getPkRecord().getName().length();
		if (l > length)
			length = l;
		return length;
	}

	private String getSqlType(String type) {
		DataType dt = DataType.fromString(type);
		if (dt == null)
			dt = DataType.TEXT;
		return dt.getSqlType();
	}
}
