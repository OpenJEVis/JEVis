-- remove deleted objects

delete from object where deletets IS NOT NULL;
delete from attribute where object not in (select o.id from object o);
delete from relationship where startobject not in (select o.id from object o);
delete from relationship where endobject not in (select o.id from object o);