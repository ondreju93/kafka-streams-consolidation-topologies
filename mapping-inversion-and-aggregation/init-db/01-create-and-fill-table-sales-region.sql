USE legacy_sales;
CREATE TABLE IF NOT EXISTS SALES_REGION (
  REGION_ID SERIAL PRIMARY KEY,
  REGION_NAME VARCHAR(40) NOT NULL,
  PARENT_REGION_ID INTEGER,
  REGION_HIERARCHY_LEVEL INTEGER NOT NULL,
  AGENT_LAST_NAME VARCHAR(40) NOT NULL,
  AGENT_FIRST_NAME VARCHAR(40) NOT NULL,
  AGENT_EMAIL_ADDRESS VARCHAR(40) NOT NULL
);

USE legacy_sales;
INSERT INTO SALES_REGION(REGION_ID, REGION_NAME, PARENT_REGION_ID, REGION_HIERARCHY_LEVEL, AGENT_LAST_NAME, AGENT_FIRST_NAME, AGENT_EMAIL_ADDRESS) VALUES
  (1, 'Trondheim', 11, 2, 'Bergman', 'John', 'jbergman@supersales.eu'),
  (2, 'Oslo', 11, 2, 'Bergman', 'John A', 'jbergman@supersales.eu'),
  (11, 'Norway', -1, 1, 'Bergman', 'John A', 'jbergman@supersales.eu'),
  (3, 'Munich', 22, 2, 'Heideger', 'Martin', 'mheideger@supersales.eu'),
  (4, 'Harburg', 22, 2, 'Mueller', 'Joseph', 'jmueller@supersales.eu'),
  (5, 'Berlin', 22, 2, 'Mueller', 'Joseph', 'jmueller@supersales.eu'),
  (22, 'Germany', -1, 1, 'Neuer', 'Franz', 'fneuer@supersales.eu'),
  (6, 'Warszawa', 33, 2, 'Przybysz', 'Karol', 'kprzybysz@supersales.eu'),
  (7, 'Poznan', 33, 2, 'Nowak', 'Jan', 'jnowak@supersales.eu'),
  (33, 'Poland', -1, 1, 'Nowak', 'Jan', 'jnowak@supersales.eu');