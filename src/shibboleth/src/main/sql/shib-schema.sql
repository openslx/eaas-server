CREATE SCHEMA shib;
   
CREATE SEQUENCE hibernate_sequence
	AS BIGINT
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE;
    
CREATE SEQUENCE shib.hibernate_sequence
	AS BIGINT
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE;

CREATE TABLE shib.adminuser_role (
    admin_user_id bigint PRIMARY KEY NOT NULL,
    role_id bigint NOT NULL
);

CREATE TABLE shib.adminusertable (
    id bigint PRIMARY KEY NOT NULL,
    description character varying(512),
    password character varying(256) NOT NULL,
    username character varying(256) NOT NULL
);

CREATE TABLE shib.agreement_text (
    id bigint PRIMARY KEY NOT NULL,
    agreement character varying(32672),
    name character varying(128),
    policy_id bigint
);

CREATE TABLE shib.audit_detail (
    id bigint PRIMARY KEY NOT NULL,
    audit_action character varying(128),
    auditstatus character varying(255),
    end_time timestamp,
    audit_log character varying(1024),
    audit_object character varying(256),
    audit_subject character varying(256),
    auditentry_id bigint
);

CREATE TABLE shib.audit_entry (
    id bigint PRIMARY KEY NOT NULL,
    audit_detail character varying(1024),
    end_time timestamp,
    audit_executor character varying(64),
    audit_name character varying(256),
    start_time timestamp,
    registry_id bigint
);

CREATE TABLE shib.business_rule (
    id bigint PRIMARY KEY NOT NULL,
    base_name character varying(128),
    name character varying(128),
    rule_text character varying(32672),
    rule_type character varying(32)
);

CREATE TABLE shib.federation (
    id bigint PRIMARY KEY NOT NULL,
    entity_category_filter character varying(512),
    entity_id character varying(2048),
    federation_metadata_url character varying(2048),
    federation_name character varying(128),
    polled_at timestamp
);

CREATE TABLE shib.idpmetadata (
    id bigint PRIMARY KEY NOT NULL,
    entity_id character varying(2048),
    entity_desc character varying(32672),
    org_name character varying(512),
    federation_id bigint
);

CREATE TABLE shib.idpmetadata_scope (
    id bigint PRIMARY KEY NOT NULL,
    regex boolean,
    scope character varying(512) NOT NULL,
    idp_id bigint
);

CREATE TABLE shib.image (
    id bigint PRIMARY KEY NOT NULL,
    imagetype character varying(255),
    name character varying(128),
    imagedata_id bigint
);

CREATE TABLE shib.image_data (
    id bigint PRIMARY KEY NOT NULL,
    image_data bigint
);

CREATE TABLE shib.policy (
    id bigint PRIMARY KEY NOT NULL,
    mandatory boolean,
    name character varying(128),
    actualagreement_id bigint,
    service_id bigint
);

CREATE TABLE shib.registry (
    id bigint PRIMARY KEY NOT NULL,
    agreed_time timestamp,
    approval_bean character varying(256),
    last_full_recon timestamp,
    last_recon timestamp,
    register_bean character varying(256) NOT NULL,
    registrystatus character varying(255),
    service_id bigint,
    user_id bigint
);

CREATE TABLE shib.registry_agreementtext (
    registry_id bigint PRIMARY KEY NOT NULL,
    agreementtext_id bigint NOT NULL
);


CREATE TABLE shib.registry_value (
    registryentity_id bigint PRIMARY KEY NOT NULL,
    value_data character varying(2048),
    key_data character varying(128) NOT NULL
);


CREATE TABLE shib.role (
    dtype character varying(31) NOT NULL,
    id bigint PRIMARY KEY NOT NULL,
    name character varying(64) NOT NULL,
    approval_bean character varying(255)
);

CREATE TABLE shib.samlmetadata (
    id bigint PRIMARY KEY NOT NULL,
    entity_id character varying(2048)
);


--CREATE TABLE shib.samlspconfigurationentity_hostnamelist (
--    samlspconfigurationentity_id bigint NOT NULL,
--    hostnamelist character varying(255)
--);

CREATE TABLE shib.serialtable (
    id bigint PRIMARY KEY NOT NULL,
    actual bigint,
    name character varying(256) NOT NULL
);

CREATE TABLE shib.service (
    id bigint PRIMARY KEY NOT NULL,
    description character varying(32672),
    name character varying(128) NOT NULL,
    published boolean,
    register_bean character varying(256) NOT NULL,
    short_description character varying(2048),
    short_name character varying(32) NOT NULL,
    accessrule_id bigint,
    adminrole_id bigint,
    approverrole_id bigint,
    image_id bigint
);


CREATE TABLE shib.service_properties (
    serviceentity_id bigint PRIMARY KEY NOT NULL,
    value_data character varying(2048),
    key_data character varying(128) NOT NULL
);

CREATE TABLE shib.spconfig (
    id bigint PRIMARY KEY NOT NULL,
    entity_id character varying(2048),
    federation_id bigint,
    acs character varying(2048),
    certificate character varying(32672),
    ecp character varying(2048),
    private_key character varying(32672),
    hostnamelist character varying(32672),
    enabled boolean
);

CREATE TABLE shib.spmetadata (
    id bigint PRIMARY KEY NOT NULL,
    entity_id character varying(2048),
    federation_id bigint
);

CREATE TABLE shib.user_attribute_store (
    userentity_id bigint NOT NULL,
    value_data character varying(2048),
    key_data character varying(1024) NOT NULL
);

CREATE TABLE shib.user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);

CREATE TABLE shib.user_secondary_group (
    user_id bigint NOT NULL,
    group_id bigint NOT NULL
);

CREATE TABLE shib.user_store (
    userentity_id bigint PRIMARY KEY NOT NULL,
    value_data character varying(2048),
    key_data character varying(128) NOT NULL
);

CREATE TABLE shib.usertable (
    id bigint PRIMARY KEY NOT NULL,
    email character varying(1024),
    eppn character varying(1024) NOT NULL,
    given_name character varying(256),
    last_update timestamp,
    persistent_id character varying(1024),
    persistent_idpid character varying(1024),
    persistent_spid character varying(1024),
    sur_name character varying(256),
    theme character varying(128),
    uid_number integer NOT NULL,
    userstatus character varying(255),
    primarygroup_id bigint
);

CREATE TABLE shib.group_store (
    id bigint PRIMARY KEY NOT NULL,
    gid_number integer NOT NULL,
    group_name character varying(255) NOT NULL,
    group_prefix character varying(255)
);
