/*
 *
 * MIT License
 *
 * Copyright (c) 2024 Hydrologic Engineering Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE
 * SOFTWARE.
 */

package cwms.cda.api.timeseriesprofile;

import static cwms.cda.api.Controllers.CREATE;
import static cwms.cda.api.Controllers.FAIL_IF_EXISTS;
import static cwms.cda.data.dao.JooqDao.getDslContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import cwms.cda.data.dao.timeseriesprofile.TimeSeriesProfileParserDao;
import cwms.cda.data.dto.timeseriesprofile.TimeSeriesProfileParserColumnar;
import cwms.cda.data.dto.timeseriesprofile.TimeSeriesProfileParserIndexed;
import cwms.cda.formatters.Formats;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiParam;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;


public final class TimeSeriesProfileParserCreateController extends TimeSeriesProfileParserBase implements Handler {

    public TimeSeriesProfileParserCreateController(MetricRegistry metrics) {
        tspMetrics(metrics);
    }

    @OpenApi(
        queryParams = {
            @OpenApiParam(name = FAIL_IF_EXISTS, type = boolean.class, description = "If true, the parser will"
                + " fail to save if the TimeSeriesProfileParser already exists"),
        },
        requestBody = @OpenApiRequestBody(
            content = {
                @OpenApiContent(from = TimeSeriesProfileParserIndexed.class, type = Formats.JSONV1),
                @OpenApiContent(from = TimeSeriesProfileParserColumnar.class, type = Formats.JSONV1),
            },
            required = true
        ),
        method = HttpMethod.POST,
        summary = "Parse TimeSeriesProfileData into a new Time Series Profile",
        tags = {TAG}
    )
    @Override
    public void handle(@NotNull Context ctx) {
        try (final Timer.Context ignored = markAndTime(CREATE)) {
            DSLContext dsl = getDslContext(ctx);

            boolean failIfExists = ctx.queryParamAsClass(FAIL_IF_EXISTS, Boolean.class).getOrDefault(true);

            TimeSeriesProfileParserColumnar tspParserColumnar = null;
            TimeSeriesProfileParserIndexed tspParserIndexed = null;
            TimeSeriesProfileParserDao tspParserDao = getParserDao(dsl);
            if (ctx.body().contains("columnar-timeseries-profile-parser")) {
                tspParserColumnar = Formats.parseContent(Formats.parseHeader(Formats.JSONV1,
                        TimeSeriesProfileParserColumnar.class), ctx.body(), TimeSeriesProfileParserColumnar.class);
            } else {
                tspParserIndexed = Formats.parseContent(Formats.parseHeader(Formats.JSONV1,
                        TimeSeriesProfileParserIndexed.class), ctx.body(), TimeSeriesProfileParserIndexed.class);
            }
            if (tspParserColumnar != null) {
                tspParserDao.storeTimeSeriesProfileParser(tspParserColumnar, failIfExists);
            } else {
                tspParserDao.storeTimeSeriesProfileParser(tspParserIndexed, failIfExists);
            }
            ctx.status(HttpServletResponse.SC_CREATED);
        }
    }
}