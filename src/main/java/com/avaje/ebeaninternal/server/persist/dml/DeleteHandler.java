package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.OptimisticLockException;

import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * Delete bean handler.
 */
public class DeleteHandler extends DmlHandler {

	private final DeleteMeta meta;

	public DeleteHandler(PersistRequestBean<?> persist, DeleteMeta meta) {
		super(persist, meta.isEmptyStringAsNull());
		this.meta = meta;
	}

	/**
	 * Generate and bind the delete statement.
	 */
	public void bind() throws SQLException {
		
		sql = meta.getSql(persistRequest);
		
		SpiTransaction t = persistRequest.getTransaction();
		boolean isBatch = t.isBatchThisRequest();

		PreparedStatement pstmt;
		if (isBatch) {
			pstmt = getPstmt(t, sql, persistRequest, false);

		} else {
			logSql(sql);
			pstmt = getPstmt(t, sql, false);
		}
		dataBind = new DataBind(pstmt);

		bindLogAppend("Binding Delete [");
		bindLogAppend(meta.getTableName());
		bindLogAppend("] where[");
		
		meta.bind(persistRequest, this);
		
		bindLogAppend("]");
		
		// log the binding to transaction log if requested
		logBinding();
	}

	/**
	 * Execute the delete non-batch.
	 */
	public void execute() throws SQLException, OptimisticLockException {
		int rowCount = dataBind.executeUpdate();
		checkRowCount(rowCount);
	}
	
    @Override
    public boolean isIncluded(BeanProperty prop) {        
        return prop.isDbUpdatable() && super.isIncluded(prop);
    }
    
    @Override
    public boolean isIncludedWhere(BeanProperty prop) {
        return prop.isDbUpdatable() && (loadedProps == null || loadedProps.contains(prop.getName()));    
    }

    public void registerDerivedRelationship(DerivedRelationshipData assocBean) {
	    throw new RuntimeException("Never called on delete");
    }
    
}
