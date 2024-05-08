package com.quincus.shipment.impl.repository.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class LocationQuery {

    public static final String COUNTRY_LOCATION_FIELD = """
             country.id as countryId, country.type as countryType, country.organizationId as countryOrganizationId
            , country.name as countryName, country.description as countryDescription, country.code as countryCode
            , country.externalId as countryExternalId, country.timezone as countryTimezone
            """ ;
    public static final String COUNTRY_GROUP_BY_LOCATION_FIELD = """
             country.id , country.type , country.organizationId, country.code
            , country.name , country.description , country.externalId , country.timezone
            """ ;
    public static final String STATE_LOCATION_FIELD = """
             state.id as stateId, state.type as stateType, state.organizationId as stateOrganizationId
            , state.name as stateName, state.description as stateDescription, state.code as stateCode
            , state.externalId as stateExternalId, state.timezone as stateTimezone
            """ ;
    public static final String STATE_GROUP_BY_LOCATION_FIELD = """
             state.id , state.type , state.organizationId, state.code
            , state.name , state.description , state.externalId , state.timezone
            """ ;
    public static final String CITY_LOCATION_FIELD = """
             city.id as cityId, city.type as cityType, city.organizationId as cityOrganizationId
            , city.name as cityName, city.description as cityDescription, city.code as cityCode
            , city.externalId as cityExternalId, city.timezone as cityTimezone
            """ ;
    public static final String CITY_GROUP_BY_LOCATION_FIELD = """
             city.id , city.type , city.organizationId, city.code
            , city.name , city.description , city.externalId , city.timezone
            """ ;
    public static final String FACILITY_LOCATION_FIELD = """
             facility.id as facilityId, facility.type as facilityType, facility.organizationId as facilityOrganizationId
            , facility.name as facilityName, facility.description as facilityDescription, facility.code as facilityCode
            , facility.externalId as facilityExternalId, facility.timezone as facilityTimezone
            """ ;
    public static final String FACILITY_GROUP_BY_LOCATION_FIELD = """
             facility.id , facility.type , facility.organizationId, facility.code
            , facility.name , facility.description , facility.externalId , facility.timezone
            """ ;
}
