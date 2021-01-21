USE ${hiveDB};

-- TODO: delete table if exists

-- creates a table with the unique combinations of collections data
CREATE TABLE occurrence_collections STORED AS parquet AS
SELECT DISTINCT ownerInstitutionCode, institutionId, institutionCode, collectionCode, collectionId, datasetKey,
CASE WHEN datasetKey = '4fa7b334-ce0d-4e88-aaae-2e0c138d049e' THEN 'US' ELSE publishingCountry END AS country
FROM ${occurrenceTable};

