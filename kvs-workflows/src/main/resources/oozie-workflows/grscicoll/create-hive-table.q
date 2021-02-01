USE ${hiveDB};

DROP TABLE IF EXISTS ${targetTable};

-- creates a table with the unique combinations of collections data
CREATE TABLE ${targetTable} STORED AS parquet AS
SELECT DISTINCT ownerInstitutionCode, institutionId, institutionCode, collectionCode, collectionId, datasetKey,
CASE WHEN datasetKey = '4fa7b334-ce0d-4e88-aaae-2e0c138d049e' THEN 'US' ELSE publishingCountry END AS country
FROM ${occurrenceTable};

