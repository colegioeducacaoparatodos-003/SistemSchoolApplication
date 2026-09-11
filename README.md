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

API para validação do BI : https://consulta.edgarsingui.ao/consultar/006987033BO043/bilhete
API para validação do NIF : https://consulta.edgarsingui.ao/consultar/006987033BO043/nif

-- 1. Apagar as tabelas que referenciam evaluation
DROP TABLE IF EXISTS grade;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS discipline;
DROP TABLE IF EXISTS trimester_result;

-- 2. Agora sim, apagar a tabela pai
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS teacher;


INSERT INTO discipline (discipline_code, discipline_name, description, status, obs, created_at, updated_at) VALUES
('DISC-2026-00001', 'Língua Portuguesa', 'Disciplina de Língua Portuguesa do Ensino Primário.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00002', 'Matemática', 'Disciplina de Matemática do Ensino Primário.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00003', 'Estudo do Meio', 'Disciplina de Estudo do Meio do Ensino Primário.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00004', 'Educação Moral e Cívica', 'Disciplina de formação moral, ética e cidadania.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00005', 'Educação Física', 'Disciplina de desenvolvimento físico, motor e desportivo.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00006', 'Educação Musical', 'Disciplina de iniciação e formação musical.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00007', 'Educação Plástica', 'Disciplina de expressão artística e plástica.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00008', 'Língua Inglesa', 'Disciplina de iniciação à língua inglesa.', 'ATIVO', NULL, NOW(), NOW()),

('DISC-2026-00009', 'Língua Portuguesa', 'Disciplina de Língua Portuguesa do I Ciclo do Ensino Secundário.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00010', 'Matemática', 'Disciplina de Matemática do I Ciclo do Ensino Secundário.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00011', 'Língua Inglesa', 'Disciplina de Língua Inglesa do I Ciclo.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00012', 'Língua Francesa', 'Disciplina de Língua Francesa do I Ciclo.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00013', 'História', 'Disciplina de História de Angola, África e do mundo.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00014', 'Geografia', 'Disciplina de Geografia física, humana e económica.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00015', 'Ciências da Natureza', 'Disciplina de estudo dos seres vivos, ambiente e fenómenos naturais.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00016', 'Física', 'Disciplina de introdução aos fenómenos físicos e suas aplicações.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00017', 'Química', 'Disciplina de introdução ao estudo da matéria e suas transformações.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00018', 'Educação Moral e Cívica', 'Disciplina de formação ética, moral e cidadania.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00019', 'Educação Física', 'Disciplina de desenvolvimento físico, motor e desportivo.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00020', 'Educação Visual e Plástica', 'Disciplina de expressão artística, visual e plástica.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00021', 'Educação Musical', 'Disciplina de formação e expressão musical.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00022', 'Informática', 'Disciplina de introdução às tecnologias de informação e comunicação.', 'ATIVO', NULL, NOW(), NOW()),
('DISC-2026-00023', 'Empreendedorismo', 'Disciplina de desenvolvimento de competências empreendedoras.', 'ATIVO', NULL, NOW(), NOW());