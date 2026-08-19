<<<<<<< HEAD
railway-verify=87e0bc587aa380345a361838afba697f14d14144c3c627847adf067daa8e83b9
3p0h6wzu.up.railway.app
=======
# SistemSchoolApplication
>>>>>>> 46323a00b379fbe6669912d70ecfc048bcc521ba


UPDATE enrolment SET shift = CASE shift
    WHEN 'MORNING' THEN 'Manhã'
    WHEN 'AFTERNOON' THEN 'Tarde'
    WHEN 'EVENING' THEN 'Noite'
    WHEN 'FULL_TIME' THEN 'Tempo Integral'
    ELSE shift END;

UPDATE enrolment SET enrolment_Type = CASE enrolment_Type
    WHEN 'ENROLMENT' THEN 'Matrícula'
    WHEN 'CONFIRMATION' THEN 'Confirmação'
    ELSE enrolment_Type END;