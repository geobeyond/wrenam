/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2016 ForgeRock AS.
 */
package org.forgerock.openam.cts.impl.query.worker.queries;

import static org.forgerock.openam.utils.Time.getCalendarInstance;

import javax.inject.Inject;
import java.util.Calendar;

import org.forgerock.openam.cts.CoreTokenConfig;
import org.forgerock.openam.sm.datalayer.api.ConnectionType;
import org.forgerock.openam.sm.datalayer.api.DataLayer;
import org.forgerock.openam.sm.datalayer.api.query.QueryBuilder;
import org.forgerock.openam.sm.datalayer.api.query.QueryFactory;
import org.forgerock.openam.tokens.CoreTokenField;
import org.forgerock.util.Reject;
import org.forgerock.util.query.QueryFilter;

/**
 * A query that selects all CTS tokens whose expiry date field is prior to the current timestamp (e.g. who have
 * exceeded their maximum expiry time).
 */
public class CTSWorkerPastExpiryDateQuery<C, F> extends CTSWorkerBaseQuery {

    private final QueryFactory<C, F> queryFactory;
    private final int pageSize;

    @Inject
    public CTSWorkerPastExpiryDateQuery(@DataLayer(ConnectionType.CTS_WORKER) QueryFactory queryFactory,
                                        CoreTokenConfig config) {
        Reject.ifTrue(config.getCleanupPageSize() <= 0);

        this.queryFactory = queryFactory;
        this.pageSize = config.getCleanupPageSize();
    }

    @Override
    public QueryBuilder getQuery() {
        Calendar now = getCalendarInstance();

        QueryFilter<CoreTokenField> filter = QueryFilter.lessThan(CoreTokenField.EXPIRY_DATE, now);

        return queryFactory.createInstance()
                .withFilter(filter.accept(queryFactory.createFilterConverter(), null))
                .pageResultsBy(pageSize)
                .returnTheseAttributes(CoreTokenField.TOKEN_ID, CoreTokenField.ETAG);
    }

}