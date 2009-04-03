-- generated by Jailer, Fri Apr 03 11:59:58 CEST 2009 from wisser@u19
-- 
-- Extraction Model:  EMPLOYEE where T.NAME='SCOTT' (/data/workspace/trunk/src/test/single-row-cycle/unrestricted_export/extractionmodel.csv)
-- Database URL:      jdbc:postgresql://localhost:5432/postgres
-- Database User:     postgres
-- Exported Rows:     43
--     DEPARTMENT                     3
--     EMPLOYEE                       14
--     SALARYGRADE                    5
--     PROJECT_PARTICIPATION          14
--     PROJECT                        4
--     ROLE                           3
-- 
-- 
-- Tabu-tables:  {  }
-- 
-- Deleted Entities: 43
--      DEPARTMENT                     3
--      EMPLOYEE                       14
--      SALARYGRADE                    5
--      PROJECT_PARTICIPATION          14
--      PROJECT                        4
--      ROLE                           3



Delete from SALARYGRADE Where GRADE in (4, 1, 5, 2, 3);
Delete from PROJECT_PARTICIPATION Where (PROJECTNO, EMPNO, START_DATE) in (values (1001, 7788, '2006-05-15'), (1002, 7876, '2006-08-22'), (1003, 7566, '2007-02-24'), (1001, 7369, '2006-01-01'), (1001, 7902, '2006-01-01'), (1002, 7782, '2006-08-22'), (1002, 7934, '2007-01-01'), (1003, 7900, '2007-02-24'), (1004, 7499, '2008-01-01'), (1004, 7521, '2008-05-01'));
Delete from PROJECT_PARTICIPATION Where (PROJECTNO, EMPNO, START_DATE) in (values (1004, 7654, '2008-04-15'), (1004, 7844, '2008-02-01'), (1004, 7900, '2008-03-01'), (1004, 7900, '2008-05-20'));
Delete from PROJECT Where PROJECTNO in (1001, 1002, 1003, 1004);
Delete from ROLE Where ROLE_ID in (100, 102, 101);
Delete from EMPLOYEE Where EMPNO in (7499, 7521, 7654, 7844, 7900, 7934, 7369, 7876);
Delete from EMPLOYEE Where EMPNO in (7698, 7782, 7788, 7902);
Delete from EMPLOYEE Where EMPNO in (7566);
Delete from DEPARTMENT Where DEPTNO in (30);
Delete from DEPARTMENT Where DEPTNO in (20);
Delete from EMPLOYEE Where EMPNO in (7839);
Delete from DEPARTMENT Where DEPTNO in (10);
