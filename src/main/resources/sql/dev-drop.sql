
    set client_min_messages = WARNING;

    alter table if exists google_conference 
       drop constraint if exists FK8oy0ehfgvxykqty0q9o7jiuvv;

    alter table if exists google_conference 
       drop constraint if exists FK1lvjtesr9bd09yq22l12uxdpp;

    alter table if exists principal 
       drop constraint if exists FK1jc8h59dwy9b7poy3bd8v40d3;

    drop table if exists google_conference cascade;

    drop table if exists principal cascade;

    drop table if exists server cascade;
