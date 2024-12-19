insert into candidate (id, version, email) values ('23471ab3-f639-4d2b-9541-7227f4ea7ee6', 1, 'foo@bar.com');

insert into question (id, version, content, title, description) values ('a7e00ff8-da41-4624-b31c-1b13c3f2e3ae', 1, 'foo bar', 'q1', 'lorem ipsum');
insert into question (id, version, content, title, description) values ('8a4b213c-ca81-4c38-b56d-d7028c2dde88', 1, 'foo buzz', 'q2', 'lorem ipsum');

insert into assessment(id, version, candidate_id) values ('46b153f4-23fd-462f-8430-fbe67b83caab', 1, '23471ab3-f639-4d2b-9541-7227f4ea7ee6');

insert into ASSESSMENT_QUESTIONS (assessment_id, question_id) values ('46b153f4-23fd-462f-8430-fbe67b83caab', 'a7e00ff8-da41-4624-b31c-1b13c3f2e3ae');

insert into ASSESSMENT_QUESTIONS (assessment_id, question_id) values ('46b153f4-23fd-462f-8430-fbe67b83caab', '8a4b213c-ca81-4c38-b56d-d7028c2dde88');


INSERT INTO team (id, version, name) VALUES ('b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 1, 'ABC');
INSERT INTO team (id, version, name) VALUES ('6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 1, 'XYZ');
INSERT INTO team (id, version, name) VALUES ('c3a8a7b1-f2d9-48c0-86ea-f215c2e6b3a3', 1, 'DEF');
INSERT INTO team (id, version, name) VALUES ('8f6b61e7-efb2-4de7-b8ed-7438c9d8babe', 1, 'GHI');


INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('123e4567-e89b-12d3-a456-426614174000', 1, 'AÑO_NUEVO', 1, 1, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('223e4567-e89b-12d3-a456-426614174001', 1, 'LUNES_CARNAVAL', 2, 12, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('323e4567-e89b-12d3-a456-426614174002', 1, 'MARTES_CARNAVAL', 2, 13, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('423e4567-e89b-12d3-a456-426614174003', 1, 'VIERNES_SANTO', 3, 29, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('523e4567-e89b-12d3-a456-426614174004', 1, 'DIA_DEL_TRABAJADOR', 5, 1, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('623e4567-e89b-12d3-a456-426614174005', 1, 'DIA_DE_LA_INDEPENDENCIA', 8, 6, 1, 1, 'FIXED');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('723e4567-e89b-12d3-a456-426614174006', 1, 'NAVIDAD', 12, 25, 1, 1, 'FIXED');


INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('823e4567-e89b-12d3-a456-426614174007', 1, 'DIA_DEL_ESTADO_PLURINACIONAL', 1, 21, 1, 30, 'MOVABLE');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('923e4567-e89b-12d3-a456-426614174008', 1, 'CORPUS_CHRISTI', 5, 30, 1, 30, 'MOVABLE');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('a23e4567-e89b-12d3-a456-426614174009', 1, 'AÑO_NUEVO_ANDINO', 6, 21, 1, 30, 'MOVABLE');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('b23e4567-e89b-12d3-a456-42661417400a', 1, 'ANIVERSARIO_DEPARTAMENTAL', 9, 14, 1, 30, 'MOVABLE');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400b', 1, 'DIA_DE_TODOS_LOS_DIFUNTOS', 11, 2, 1, 30, 'MOVABLE');


INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400c', 1, 'CUMPLEAÑOS', 12, 31, 0.5, 'OTHER');
INSERT INTO vacation (id, version, category, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400d', 1, 'MATERNIDAD', 90, 90, 'OTHER');
INSERT INTO vacation (id, version, category, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400e', 1, 'PATERNIDAD', 3, 3, 'OTHER');
INSERT INTO vacation (id, version, category, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400f', 1, 'MATRIMONIO', 3, 3, 'OTHER');
INSERT INTO vacation (id, version, category, duration, expiration, type) VALUES ('550e8400-e29b-41d4-a716-446655440000', 1, 'DUELO_1ER_GRADO', 3, 3, 'OTHER');
INSERT INTO vacation (id, version, category, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417400a', 1, 'DUELO_2ER_GRADO', 2, 2, 'OTHER');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417401a', 1, 'DIA_DEL_PADRE', 3, 19, 0.5, 30, 'OTHER');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417401b', 1, 'DIA_DE_LA_MADRE', 5, 27, 0.5, 30, 'OTHER');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417401c', 1, 'DIA_DE_LA_MUJER_INTERNACIONAL', 3, 8, 0.5, 30, 'OTHER');
INSERT INTO vacation (id, version, category, month_of_year, day_of_month, duration, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417401d', 1, 'DIA_DE_LA_MUJER_NACIONAL', 10, 11, 0.5, 30, 'OTHER');
INSERT INTO vacation (id, version, category, duration, type) VALUES ('c23e4567-e89b-12d3-a456-42661417401e', 1, 'PERMISOS_DE_SALUD', 2, 'OTHER');
INSERT INTO vacation (id, version, category, expiration, type) VALUES ('490e5fbe-895b-42f8-b914-95437f7b39c0', 1, 'VACACION_GESTION_ACTUAL', 360, 'OTHER');
INSERT INTO vacation (id, version, category, expiration, type) VALUES ('c23e4567-e89b-12d3-a456-4266141740ff', 1, 'VACACION_GESTION_ANTERIOR', 360, 'OTHER');


insert into employee (id, version, username, first_name, last_name, status, team_id, gender, birthday, date_of_entry,lead_manager) values ('5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 1, 'bob', 'Bob', 'Test', 'ACTIVE','b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 'MALE', '2024-02-20', '2013-10-22',1);
insert into employee (id, version, username, first_name, last_name, status, team_id, gender, date_of_entry) values ('cba3efb7-32bc-44be-9fdc-fc5e4f211254', 1, 'ben', 'Ben', 'Test', 'ACTIVE', '6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 'MALE', '2016-10-23');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender, date_of_entry) values ('e99b7af5-7d3a-4c0f-b8bc-e8d0388d8fc4', 1, 'jperez', 'Juan', 'Perez Condori', 'INACTIVE', 'c3a8a7b1-f2d9-48c0-86ea-f215c2e6b3a3', 'MALE', '2022-10-22');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender, date_of_entry) values ('f6ab3c6d-7078-45f6-9b22-4e37637bfec6', 1, 'agarcia', 'Ana', 'Garcia Rojas', 'ACTIVE', '8f6b61e7-efb2-4de7-b8ed-7438c9d8babe', 'FEMALE', '2024-10-24');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('2e2293b1-3f9a-4f3d-abc8-32639b0a5e15', 1, 'clopez', 'Carlos', 'Lopez Mendoza', 'INACTIVE', 'b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('4b1c6c35-4627-4b35-b6e9-dc75c68b2c31', 1, 'mfernandez', 'Maria', 'Fernandez Villca', 'ACTIVE', '6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('afc5c741-f70a-4394-853b-39d51b118927', 1, 'lgutierrez', 'Luis', 'Gutierrez Mamani', 'ACTIVE', 'c3a8a7b1-f2d9-48c0-86ea-f215c2e6b3a3', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('b2436b82-7b9f-4f0d-9463-f2c3173a45c3', 1, 'lmartinez', 'Laura', 'Martinez Paredes', 'INACTIVE', '8f6b61e7-efb2-4de7-b8ed-7438c9d8babe', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('6e6a8a4e-9f6b-44eb-8c69-40acfdc86756', 1, 'rsantos', 'Roberto', 'Santos Escobar', 'ACTIVE','b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('36b0d1c6-bdc0-4d98-94bb-08b9bce3f0d5', 1, 'vmorales', 'Valeria', 'Morales Ochoa', 'INACTIVE', '6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('5a1c6d80-58b3-43e3-a5a5-24b4a2d1d54a', 1, 'jramirez', 'Jorge', 'Ramirez Tapia', 'ACTIVE', 'c3a8a7b1-f2d9-48c0-86ea-f215c2e6b3a3', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('9d6a5b2e-6d0b-4b89-8d6a-d3f3d1bfc047', 1, 'storres', 'Sandra', 'Torres Huanca', 'ACTIVE', '8f6b61e7-efb2-4de7-b8ed-7438c9d8babe', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('f8b3e0c0-0d5a-4e5c-bf9d-207b9b5e8279', 1, 'fquispe', 'Felipe', 'Quispe Huanca', 'INACTIVE','b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('cd80e1d0-9a08-44a6-bd63-2c63eaa003d4', 1, 'grivas', 'Gabriela', 'Rivas Arana', 'ACTIVE', '6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('62d3c1b7-815e-4e96-8d7e-f8c4236bca55', 1, 'oflores', 'Oscar', 'Flores Quiroga', 'INACTIVE', 'c3a8a7b1-f2d9-48c0-86ea-f215c2e6b3a3', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('f20b7c5a-5a67-44f0-9ec1-4c1b8e80de05', 1, 'mvargas', 'Marta', 'Vargas Soria', 'ACTIVE', '8f6b61e7-efb2-4de7-b8ed-7438c9d8babe', 'FEMALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('19b5a76e-d7b1-4b76-8b02-4d0748e85809', 1, 'aespinoza', 'Andres', 'Espinoza Chura', 'INACTIVE','b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa', 'MALE');
insert into employee (id, version, username, first_name, last_name, status, team_id, gender) values ('5c1a7b82-832d-4f24-8377-54b77b91b6a8', 1, 'cvillanueva', 'Carla', 'Villanueva Arce', 'ACTIVE', '6d63bc15-3f8b-46f7-9cf1-7e9b0b9a2b28', 'FEMALE');


insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9d6f12ba-e341-4e7a-b8a6-cab0982bd8c1', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'PATERNIDAD', 'APROBADO', 3, '2024-10-03', '2024-10-01', '2024-10-03', 3, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9a6f12ba-e111-4e7a-b8a6-caa0982bd8a1', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'AÑO_NUEVO', 'APROBADO', 1, '2024-01-01', '2024-01-01', '2024-01-01', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9b6f12ba-e222-4e7a-b8a6-caa0982bd8b2', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'LUNES_CARNAVAL', 'APROBADO', 1, '2025-02-12', '2025-02-12', '2025-02-12', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9c6f12ba-e333-4e7a-b8a6-caa0982bd8c3', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'MARTES_CARNAVAL', 'SOLICITADO', 1, '2025-02-13', '2025-02-13', '2025-02-13', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9d6f12ba-e444-4e7a-b8a6-caa0982bd8d4', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'VIERNES_SANTO', 'APROBADO', 1, '2024-03-29', '2024-03-29', '2024-03-29', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('9e6f12ba-e555-4e7a-b8a6-caa0982bd8e5', 1, '5c6f11fe-c341-4be7-a9a6-bba0081ad7c6', 'VACACION_GESTION_ANTERIOR', 'APROBADO', 20, '2024-11-01', '2022-11-01', '2022-11-30', 20, 0);

insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('8c653f2a-f9a3-4d67-b3b6-12ad98fe0983', 1, 'f6ab3c6d-7078-45f6-9b22-4e37637bfec6', 'DIA_DEL_TRABAJADOR', 'APROBADO', 1, '2024-05-01', '2024-05-01', '2024-05-01', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('fb9d9d75-b2ab-4ea4-b8b3-0a8f89e5c123', 1, '2e2293b1-3f9a-4f3d-abc8-32639b0a5e15', 'DIA_DE_LA_INDEPENDENCIA', 'APROBADO', 1, '2024-08-06', '2024-08-06', '2024-08-06', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('6fdc47a8-127b-41c4-8d12-7fc12098ab12', 1, 'b2436b82-7b9f-4f0d-9463-f2c3173a45c3', 'DIA_DE_TODOS_LOS_DIFUNTOS', 'SOLICITADO', 1, '2025-12-01', '2025-11-02', '2025-11-02', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('12ec8b74-983d-4a17-b67e-134f45ae904c', 1, '5c1a7b82-832d-4f24-8377-54b77b91b6a8', 'AÑO_NUEVO', 'SOLICITADO', 1, '2025-01-01', '2025-01-01', '2025-01-01', 1, 0);
insert into time_off_request (id, version, employee_id, category, state, available_days, expiration, start_date, end_date, days_to_be_take, days_balance)
values ('89bc4b2a-943f-487c-a9f3-bacf78145e67', 1, 'cba3efb7-32bc-44be-9fdc-fc5e4f211254', 'LUNES_CARNAVAL', 'APROBADO', 1, '2024-02-12', '2024-02-12', '2024-02-12', 1, 0);



insert into hours_worked (id, version, actividad, date, horaspendientes, hours,  total_hours,week_number,employee_id,team_id) values ('389389ce-7b2e-4f39-aa06-2a251a2b35ea	',0,'meet','2024-11-27',0,4,0,48,'5c6f11fe-c341-4be7-a9a6-bba0081ad7c6','b0e8f394-78c1-4d8a-9c57-dc6e8b36a5fa');