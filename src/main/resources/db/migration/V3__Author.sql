CREATE TABLE Author
(
    id         serial PRIMARY KEY,
    name       text      NOT NULL,
    created_at timestamp NOT NULL
);

-- Add the foreign key reference column
ALTER TABLE budget ADD COLUMN author_id INTEGER NULL;

-- Create a non-blocking foreign key constraint
ALTER TABLE budget
ADD CONSTRAINT fk_budget_author
FOREIGN KEY (author_id)
REFERENCES Author(id)
NOT VALID;

-- Validate the constraint without blocking
ALTER TABLE budget VALIDATE CONSTRAINT fk_budget_author;
