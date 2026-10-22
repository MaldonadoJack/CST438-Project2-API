
-- Issue #20: Mark a Workout as Completed
-- Add completion tracking without changing existing workout fields.

ALTER TABLE workouts
ADD COLUMN IF NOT EXISTS is_completed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE workouts
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;
