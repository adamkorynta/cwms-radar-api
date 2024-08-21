package cwms.cda.data.dao.timeseriesprofile;

import static usace.cwms.db.jooq.codegen.tables.AV_TS_PROFILE.AV_TS_PROFILE;

import cwms.cda.data.dao.JooqDao;
import cwms.cda.data.dto.CwmsId;
import cwms.cda.data.dto.timeseriesprofile.TimeSeriesProfile;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import usace.cwms.db.jooq.codegen.packages.CWMS_TS_PROFILE_PACKAGE;
import usace.cwms.db.jooq.codegen.udt.records.STR_TAB_T;
import usace.cwms.db.jooq.codegen.udt.records.TS_PROFILE_T;



public class TimeSeriesProfileDao extends JooqDao<TimeSeriesProfile> {
    public TimeSeriesProfileDao(DSLContext dsl) {
        super(dsl);
    }

    public void storeTimeSeriesProfile(TimeSeriesProfile timeSeriesProfile, boolean failIfExists) {

        connection(dsl, conn -> {
            setOffice(conn, timeSeriesProfile.getLocationId().getOfficeId());
            List<String> parameterList = timeSeriesProfile.getParameterList();
            StringBuilder parameterString = new StringBuilder(parameterList.get(0));

            for (int i = 1; i < parameterList.size(); i++) {
                parameterString.append(",").append(parameterList.get(i));
            }
            String referenceTsId = null;
            if (timeSeriesProfile.getReferenceTsId() != null) {
                referenceTsId = timeSeriesProfile.getReferenceTsId().getName();
            }
            setOffice(conn, timeSeriesProfile.getLocationId().getOfficeId());
            CWMS_TS_PROFILE_PACKAGE.call_STORE_TS_PROFILE(DSL.using(conn).configuration(), timeSeriesProfile.getLocationId().getName(),
                    timeSeriesProfile.getKeyParameter(),
                    parameterString.toString(),
                    timeSeriesProfile.getDescription(), referenceTsId, failIfExists?"T":"F", "T", timeSeriesProfile.getLocationId().getOfficeId());
        });
    }

    public TimeSeriesProfile retrieveTimeSeriesProfile(String locationId, String parameterId, String officeId) {
        return connectionResult(dsl, conn -> {
            setOffice(conn, officeId);
            TS_PROFILE_T timeSeriesProfile = CWMS_TS_PROFILE_PACKAGE.call_RETRIEVE_TS_PROFILE(
                    DSL.using(conn).configuration(), locationId, parameterId, officeId);
            return map(timeSeriesProfile, locationId, parameterId, officeId);
        });
    }

    public void deleteTimeSeriesProfile(String locationId, String keyParameter, String officeId) {
        connection(dsl, conn -> {
                setOffice(conn, officeId);
                CWMS_TS_PROFILE_PACKAGE.call_DELETE_TS_PROFILE(DSL.using(conn).configuration(), locationId, keyParameter, "DELETE ALL",
                        officeId);
        });
    }

    public void copyTimeSeriesProfile(String locationId, String keyParameter, String destinationLocation, String destRefTsId, String officeId) {
        connection(dsl, conn -> {
                setOffice(conn, officeId);
                CWMS_TS_PROFILE_PACKAGE.call_COPY_TS_PROFILE(DSL.using(conn).configuration(), locationId, keyParameter, destinationLocation,
                        destRefTsId, "F", "F",
                        officeId);
        });
    }
     public List<TimeSeriesProfile> catalogTimeSeriesProfiles(String locationIdMask, String parameterIdMask, String officeIdMask) {
         List<TimeSeriesProfile> timeSeriesProfileList = new ArrayList<>();

        Condition whereCondition = JooqDao.caseInsensitiveLikeRegexNullTrue(AV_TS_PROFILE.LOCATION_ID, locationIdMask);
        whereCondition = whereCondition.and(JooqDao.caseInsensitiveLikeRegex(AV_TS_PROFILE.OFFICE_ID, officeIdMask));
        whereCondition = whereCondition.and(JooqDao.caseInsensitiveLikeRegex(AV_TS_PROFILE.KEY_PARAMETER_ID, parameterIdMask));

         @NotNull Result<Record> timeSeriesProfileResults =  dsl.select(DSL.asterisk()).from(AV_TS_PROFILE)
                 .where(whereCondition)
                 .fetch();
         for (Record timeSeriesProfileResult : timeSeriesProfileResults) {
             String parameters = timeSeriesProfileResult.get("PARAMETERS", String.class);
             String[] parameterArray = parameters.split(",");
             List<String> parameterList = Arrays.asList(parameterArray);

             CwmsId locationId = new CwmsId.Builder()
                     .withName((String) timeSeriesProfileResult.get("LOCATION_ID"))
                     .withOfficeId((String) timeSeriesProfileResult.get("OFFICE_ID"))
                     .build();
             CwmsId referenceTsId = new CwmsId.Builder()
                     .withName((String) timeSeriesProfileResult.get("ELEV_TS_ID"))
                     .withOfficeId((String) timeSeriesProfileResult.get("OFFICE_ID"))
                     .build();
             timeSeriesProfileList.add(new TimeSeriesProfile.Builder()
                     .withDescription((String) timeSeriesProfileResult.get("DESCRIPTION"))
                     .withReferenceTsId(referenceTsId)
                     .withKeyParameter((String) timeSeriesProfileResult.get("KEY_PARAMETER_ID"))
                     .withLocationId(locationId)
                     .withParameterList(parameterList)
                     .build());
         }
         return timeSeriesProfileList;
    }

    private TimeSeriesProfile map(TS_PROFILE_T timeSeriesProfile, String locationName, String keyParameter, String officeId) {
        STR_TAB_T profileParams = timeSeriesProfile.getPROFILE_PARAMS();
        List<String> parameterList = new ArrayList<>(profileParams);
        CwmsId locationId = new CwmsId.Builder().withName(locationName).withOfficeId(officeId).build();
        CwmsId referenceTsId = null;
        if (timeSeriesProfile.getREFERENCE_TS_ID() != null) {
            referenceTsId = new CwmsId.Builder().withName(timeSeriesProfile.getREFERENCE_TS_ID()).withOfficeId(officeId).build();
        }
        return new TimeSeriesProfile.Builder()
                .withLocationId(locationId)
                .withDescription(timeSeriesProfile.getDESCRIPTION())
                .withReferenceTsId(referenceTsId)
                .withKeyParameter(keyParameter)
                .withParameterList(parameterList)
                .build();
    }
}
