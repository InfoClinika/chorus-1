package org.hibernate.dialect;

import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * @author Vitalii Petkanych
 */
public class ExtendedMySQL5Dialect extends MySQL5Dialect {
    public ExtendedMySQL5Dialect() {
        super();
        registerFunction("regexp", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 REGEXP ?2"));
    }
}
