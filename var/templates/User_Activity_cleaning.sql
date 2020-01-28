-- Delete all user activities which are olden the one year.
-- start the script daily with crontab.

delete from sample
    where attribute="Activities"
    and sample.timestamp <=DATE (now()-INTERVAL 1 YEAR)
    and object in (select id from object where type="User" and deletets is null);