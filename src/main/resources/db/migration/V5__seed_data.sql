-- =============================================================
-- V5: Datos de prueba (seed)
-- Contexto geográfico: Piedecuesta, Santander, Colombia
-- Coordenadas de referencia: 6.999754, -73.053789
-- =============================================================

-- -------------------------------------------------------------
-- 1. USUARIOS
-- Contraseña de todos: "password123"  (BCrypt de 10 rondas)
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre
-- -------------------------------------------------------------

INSERT INTO users (username, password, role, full_name, photo_url, description, latitude, longitude, address)
VALUES
-- Emprendedores
('carlos_diseno',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'Carlos Andrés Rueda',
 'https://i.pravatar.cc/150?img=11',
 'Diseñador gráfico con 5 años de experiencia. Logos, branding y piezas para redes sociales.',
 6.99801,  -73.05210, 'Calle 5 # 3-20, Piedecuesta, Santander'),

('maria_tutora',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'María Fernanda López',
 'https://i.pravatar.cc/150?img=5',
 'Licenciada en Matemáticas. Clases particulares para bachillerato y universitarios. Resultados garantizados.',
 7.00124,  -73.05487, 'Carrera 9 # 7-15, Piedecuesta, Santander'),

('pedro_reparaciones',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'Pedro Pablo Villamizar',
 'https://i.pravatar.cc/150?img=15',
 'Técnico en construcción y acabados. Reparaciones eléctricas, plomería y pintura. Trabajo garantizado.',
 6.99512,  -73.05601, 'Diagonal 12 # 5-8, Piedecuesta, Santander'),

('lucia_belleza',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'Lucía Marcela Torres',
 'https://i.pravatar.cc/150?img=9',
 'Estilista profesional. Cortes, tintes, manicura y pedicura a domicilio.',
 7.00389,  -73.05320, 'Av. El Bosque # 10-22, Piedecuesta, Santander'),

('jorge_tech',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'Jorge Iván Medina',
 'https://i.pravatar.cc/150?img=18',
 'Ingeniero de sistemas. Soporte técnico, reparación de PCs y configuración de redes.',
 6.99654,  -73.04990, 'Transversal 6 # 2-30, Piedecuesta, Santander'),

('ana_cocina',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'ENTREPRENEUR',
 'Ana Milena Castillo',
 'https://i.pravatar.cc/150?img=25',
 'Chef con formación en cocina colombiana e internacional. Catering para eventos y comidas a domicilio.',
 7.00210,  -73.05720, 'Calle 8 # 6-11, Piedecuesta, Santander'),

-- Clientes
('juanito_cliente',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'CLIENT',
 'Juan Diego Hernández',
 'https://i.pravatar.cc/150?img=33',
 NULL,
 6.99900,  -73.05400, 'Piedecuesta, Santander'),

('sofia_cliente',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'CLIENT',
 'Sofía Valentina Gómez',
 'https://i.pravatar.cc/150?img=44',
 NULL,
 7.00050,  -73.05150, 'Piedecuesta, Santander'),

('andres_cliente',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKHCNicre',
 'CLIENT',
 'Andrés Felipe Morales',
 'https://i.pravatar.cc/150?img=52',
 NULL,
 6.99600,  -73.05900, 'Piedecuesta, Santander');


-- -------------------------------------------------------------
-- 2. SERVICIOS
-- Categorías insertadas en V3:
--   1=Diseño gráfico, 2=Reparaciones del hogar, 3=Tutorías y clases
--   4=Belleza y cuidado personal, 5=Tecnología e informática
--   6=Cocina y catering, 7=Fotografía y video
--   8=Transporte y mudanzas, 9=Jardinería y limpieza, 10=Otros
-- -------------------------------------------------------------

INSERT INTO service_posts (title, description, price, latitude, longitude, address, status, entrepreneur_id, category_id)
VALUES
-- Carlos (diseño gráfico)
('Diseño de logo profesional',
 'Creación de logotipo a medida para tu negocio o emprendimiento. Incluye 3 propuestas iniciales, 2 rondas de ajustes y entrega en formatos PNG, SVG y PDF. Tiempo de entrega: 5 días hábiles.',
 150000, 6.99801, -73.05210, 'Calle 5 # 3-20, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 1),

('Kit de redes sociales',
 'Diseño de plantillas editables para Instagram, Facebook y TikTok. Pack de 10 publicaciones + 5 historias en tu paleta de colores corporativos. Entrega en formato Canva y PNG.',
 80000, 6.99801, -73.05210, 'Calle 5 # 3-20, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 1),

-- María (tutorías)
('Clases de Matemáticas — Bachillerato',
 'Refuerzo escolar en álgebra, geometría, trigonometría y cálculo básico. Clases personalizadas de 1 hora. Me adapto al ritmo y necesidades del estudiante. Disponibilidad lunes a sábado.',
 35000, 7.00124, -73.05487, 'Carrera 9 # 7-15, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'maria_tutora'),
 3),

('Preparación ICFES — Matemáticas y Razonamiento',
 'Preparación intensiva para las pruebas Saber 11. Simulacros semanales, revisión de errores y estrategias de tiempo. Grupos de máximo 3 estudiantes para atención personalizada.',
 45000, 7.00124, -73.05487, 'Carrera 9 # 7-15, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'maria_tutora'),
 3),

-- Pedro (reparaciones)
('Reparación eléctrica residencial',
 'Instalación y reparación de tomacorrientes, interruptores, puntos de luz y tableros eléctricos. Trabajo con materiales de calidad, certificado y garantía de 6 meses. Atención en Piedecuesta y alrededores.',
 60000, 6.99512, -73.05601, 'Diagonal 12 # 5-8, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'pedro_reparaciones'),
 2),

('Pintura de interiores y exteriores',
 'Pintura residencial y comercial. Preparación de superficies, aplicación de estuco y pintura de alta calidad. Presupuesto sin costo. Me desplazo a toda la zona metropolitana de Piedecuesta.',
 NULL, 6.99512, -73.05601, 'Diagonal 12 # 5-8, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'pedro_reparaciones'),
 2),

-- Lucía (belleza)
('Corte y peinado a domicilio',
 'Servicio de estilismo en la comodidad de tu hogar. Cortes para hombre, mujer y niños. Peinados para eventos especiales. Me desplazo en un radio de 5 km. Cita previa requerida.',
 40000, 7.00389, -73.05320, 'Av. El Bosque # 10-22, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 4),

('Manicura y pedicura spa',
 'Tratamiento completo de manos y pies: exfoliación, hidratación, corte, esmaltado con gel o tradicional. Incluye masaje relajante. Servicio en mi estudio o a domicilio.',
 55000, 7.00389, -73.05320, 'Av. El Bosque # 10-22, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 4),

-- Jorge (tecnología)
('Soporte técnico de computadores',
 'Diagnóstico, limpieza física, formateo, instalación de Windows/Linux, eliminación de virus y actualización de drivers. Servicio a domicilio disponible. Respuesta en menos de 24 horas.',
 70000, 6.99654, -73.04990, 'Transversal 6 # 2-30, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'jorge_tech'),
 5),

('Instalación y configuración de red WiFi',
 'Instalación de routers, repetidores y redes mesh. Configuración de seguridad WPA2/WPA3 y control parental. Optimización de señal para hogares y pequeñas empresas. Garantía de cobertura.',
 80000, 6.99654, -73.04990, 'Transversal 6 # 2-30, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'jorge_tech'),
 5),

-- Ana (cocina)
('Catering para eventos pequeños',
 'Preparación y servicio de comida para reuniones de hasta 30 personas. Menú personalizable: picadas, comida típica, opciones saludables o temáticas. Incluye vajilla desechable premium.',
 350000, 7.00210, -73.05720, 'Calle 8 # 6-11, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'ana_cocina'),
 6),

('Comidas saludables a domicilio — plan semanal',
 'Preparación de almuerzos saludables de lunes a viernes. Menú equilibrado con proteínas, carbohidratos complejos y vegetales. Opción vegetariana disponible. Entrega entre 11:30 am y 1:00 pm.',
 180000, 7.00210, -73.05720, 'Calle 8 # 6-11, Piedecuesta',
 'ACTIVE',
 (SELECT id FROM users WHERE username = 'ana_cocina'),
 6);


-- -------------------------------------------------------------
-- 3. IMÁGENES DE SERVICIOS
-- URLs de Unsplash (libres de derechos, acceso directo)
-- -------------------------------------------------------------

INSERT INTO service_images (image_url, service_post_id)
VALUES
-- Logo profesional (id=1)
('https://images.unsplash.com/photo-1626785774573-4b799315345d?w=800',
 (SELECT id FROM service_posts WHERE title = 'Diseño de logo profesional')),
('https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=800',
 (SELECT id FROM service_posts WHERE title = 'Diseño de logo profesional')),

-- Kit redes sociales (id=2)
('https://images.unsplash.com/photo-1611262588024-d12430b98920?w=800',
 (SELECT id FROM service_posts WHERE title = 'Kit de redes sociales')),

-- Clases matemáticas (id=3)
('https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800',
 (SELECT id FROM service_posts WHERE title = 'Clases de Matemáticas — Bachillerato')),
('https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800',
 (SELECT id FROM service_posts WHERE title = 'Clases de Matemáticas — Bachillerato')),

-- Preparación ICFES (id=4)
('https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800',
 (SELECT id FROM service_posts WHERE title = 'Preparación ICFES — Matemáticas y Razonamiento')),

-- Reparación eléctrica (id=5)
('https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800',
 (SELECT id FROM service_posts WHERE title = 'Reparación eléctrica residencial')),
('https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800',
 (SELECT id FROM service_posts WHERE title = 'Reparación eléctrica residencial')),

-- Pintura (id=6)
('https://images.unsplash.com/photo-1562259949-e8e7689d7828?w=800',
 (SELECT id FROM service_posts WHERE title = 'Pintura de interiores y exteriores')),

-- Corte y peinado (id=7)
('https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800',
 (SELECT id FROM service_posts WHERE title = 'Corte y peinado a domicilio')),
('https://images.unsplash.com/photo-1560066984-138dadb4c035?w=800',
 (SELECT id FROM service_posts WHERE title = 'Corte y peinado a domicilio')),

-- Manicura (id=8)
('https://images.unsplash.com/photo-1604654894610-df63bc536371?w=800',
 (SELECT id FROM service_posts WHERE title = 'Manicura y pedicura spa')),
('https://images.unsplash.com/photo-1604654894610-df63bc536371?w=800',
 (SELECT id FROM service_posts WHERE title = 'Manicura y pedicura spa')),

-- Soporte técnico (id=9)
('https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=800',
 (SELECT id FROM service_posts WHERE title = 'Soporte técnico de computadores')),
('https://images.unsplash.com/photo-1518770660439-4636190af475?w=800',
 (SELECT id FROM service_posts WHERE title = 'Soporte técnico de computadores')),

-- Red WiFi (id=10)
('https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=800',
 (SELECT id FROM service_posts WHERE title = 'Instalación y configuración de red WiFi')),

-- Catering (id=11)
('https://images.unsplash.com/photo-1555244162-803834f70033?w=800',
 (SELECT id FROM service_posts WHERE title = 'Catering para eventos pequeños')),
('https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800',
 (SELECT id FROM service_posts WHERE title = 'Catering para eventos pequeños')),

-- Comidas saludables (id=12)
('https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800',
 (SELECT id FROM service_posts WHERE title = 'Comidas saludables a domicilio — plan semanal')),
('https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800',
 (SELECT id FROM service_posts WHERE title = 'Comidas saludables a domicilio — plan semanal'));


-- -------------------------------------------------------------
-- 4. RESEÑAS
-- Tres clientes calificando distintos servicios / emprendedores.
-- Cada (client_id, service_post_id) es único por el constraint uq_review_client_service.
-- -------------------------------------------------------------

INSERT INTO reviews (rating, comment, created_at, client_id, entrepreneur_id, service_post_id)
VALUES
-- juanito sobre Carlos (logo)
(5, 'Excelente trabajo. El logo quedó exactamente como lo imaginaba, muy profesional y entregó antes del plazo.',
 NOW() - INTERVAL '10 days',
 (SELECT id FROM users WHERE username = 'juanito_cliente'),
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 (SELECT id FROM service_posts WHERE title = 'Diseño de logo profesional')),

-- juanito sobre María (clases)
(5, 'Muy buena profesora, explica con paciencia y el resultado en el colegio fue inmediato. La recomiendo.',
 NOW() - INTERVAL '5 days',
 (SELECT id FROM users WHERE username = 'juanito_cliente'),
 (SELECT id FROM users WHERE username = 'maria_tutora'),
 (SELECT id FROM service_posts WHERE title = 'Clases de Matemáticas — Bachillerato')),

-- juanito sobre Pedro (eléctrico)
(4, 'Buen trabajo, puntual y dejó todo limpio. Solo tardó un poco más de lo acordado pero el resultado fue perfecto.',
 NOW() - INTERVAL '3 days',
 (SELECT id FROM users WHERE username = 'juanito_cliente'),
 (SELECT id FROM users WHERE username = 'pedro_reparaciones'),
 (SELECT id FROM service_posts WHERE title = 'Reparación eléctrica residencial')),

-- sofia sobre Carlos (kit redes)
(4, 'Buen diseño, muy creativo. Hubiera querido un poco más de variedad en las propuestas iniciales, pero quedé satisfecha.',
 NOW() - INTERVAL '8 days',
 (SELECT id FROM users WHERE username = 'sofia_cliente'),
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 (SELECT id FROM service_posts WHERE title = 'Kit de redes sociales')),

-- sofia sobre Lucía (corte)
(5, 'Increíble servicio a domicilio. Muy profesional, puntual y el resultado fue espectacular. ¡Volveré sin duda!',
 NOW() - INTERVAL '6 days',
 (SELECT id FROM users WHERE username = 'sofia_cliente'),
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 (SELECT id FROM service_posts WHERE title = 'Corte y peinado a domicilio')),

-- sofia sobre Jorge (soporte)
(5, 'Resolvió el problema del computador en menos de una hora. Muy eficiente y el precio fue justo.',
 NOW() - INTERVAL '2 days',
 (SELECT id FROM users WHERE username = 'sofia_cliente'),
 (SELECT id FROM users WHERE username = 'jorge_tech'),
 (SELECT id FROM service_posts WHERE title = 'Soporte técnico de computadores')),

-- andres sobre Ana (catering)
(5, 'El catering para el cumpleaños de mi mamá fue un éxito total. La comida estuvo deliciosa y el servicio impecable.',
 NOW() - INTERVAL '4 days',
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'ana_cocina'),
 (SELECT id FROM service_posts WHERE title = 'Catering para eventos pequeños')),

-- andres sobre María (ICFES)
(4, 'Me preparé bien para el ICFES con María. Subí 30 puntos en matemáticas. Muy metódica y exigente, lo que necesitaba.',
 NOW() - INTERVAL '7 days',
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'maria_tutora'),
 (SELECT id FROM service_posts WHERE title = 'Preparación ICFES — Matemáticas y Razonamiento')),

-- andres sobre Lucía (manicura)
(3, 'Buen servicio aunque demoró más de lo esperado. El resultado final estuvo bien. Volvería a intentarlo.',
 NOW() - INTERVAL '1 day',
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 (SELECT id FROM service_posts WHERE title = 'Manicura y pedicura spa'));


-- -------------------------------------------------------------
-- 5. MENSAJES DE CHAT
-- Conversaciones realistas entre clientes y emprendedores.
-- -------------------------------------------------------------

INSERT INTO chat_messages (content, sent_at, is_read, sender_id, receiver_id)
VALUES
-- juanito → carlos: consulta sobre logo
('Hola Carlos, vi tu servicio de diseño de logos. ¿Manejas logos para restaurantes?',
 NOW() - INTERVAL '12 days',  TRUE,
 (SELECT id FROM users WHERE username = 'juanito_cliente'),
 (SELECT id FROM users WHERE username = 'carlos_diseno')),

('¡Hola! Claro que sí, tengo experiencia en branding para restaurantes y negocios de comida. ¿Qué tipo de restaurante tienes?',
 NOW() - INTERVAL '12 days' + INTERVAL '30 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 (SELECT id FROM users WHERE username = 'juanito_cliente')),

('Es un negocio de hamburguesas artesanales. Me gustaría algo moderno y juvenil.',
 NOW() - INTERVAL '12 days' + INTERVAL '45 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'juanito_cliente'),
 (SELECT id FROM users WHERE username = 'carlos_diseno')),

('Perfecto, es un estilo que me encanta trabajar. Te puedo mostrar algunos ejemplos similares que he hecho. ¿Cuándo podemos reunirnos virtualmente para hablar del concepto?',
 NOW() - INTERVAL '11 days',  TRUE,
 (SELECT id FROM users WHERE username = 'carlos_diseno'),
 (SELECT id FROM users WHERE username = 'juanito_cliente')),

-- sofia → lucia: cita peinado
('Buenas tardes Lucía, ¿tienes disponibilidad este sábado para un corte y brushing?',
 NOW() - INTERVAL '7 days',  TRUE,
 (SELECT id FROM users WHERE username = 'sofia_cliente'),
 (SELECT id FROM users WHERE username = 'lucia_belleza')),

('¡Hola Sofía! Sí tengo disponibilidad el sábado. Tengo espacios a las 10am y a las 2pm. ¿Cuál te queda mejor?',
 NOW() - INTERVAL '7 days' + INTERVAL '20 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 (SELECT id FROM users WHERE username = 'sofia_cliente')),

('Perfecto, me quedo con las 10am. ¿Cuál es tu dirección exacta?',
 NOW() - INTERVAL '7 days' + INTERVAL '35 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'sofia_cliente'),
 (SELECT id FROM users WHERE username = 'lucia_belleza')),

('Estoy en la Av. El Bosque # 10-22. Te comparto el pin de ubicación por WhatsApp. ¡Nos vemos el sábado!',
 NOW() - INTERVAL '7 days' + INTERVAL '40 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'lucia_belleza'),
 (SELECT id FROM users WHERE username = 'sofia_cliente')),

-- andres → jorge: soporte técnico
('Jorge, tengo el computador muy lento y creo que tiene virus. ¿Puedes venir a revisarlo?',
 NOW() - INTERVAL '3 days',  TRUE,
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'jorge_tech')),

('Hola Andrés, con gusto. ¿Qué sistema operativo tienes y cuál es el síntoma principal aparte de la lentitud?',
 NOW() - INTERVAL '3 days' + INTERVAL '15 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'jorge_tech'),
 (SELECT id FROM users WHERE username = 'andres_cliente')),

('Windows 11, y cuando lo enciendo aparece una ventana rara que no puedo cerrar.',
 NOW() - INTERVAL '3 days' + INTERVAL '25 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'jorge_tech')),

('Suena a adware o PUP. Puedo ir mañana en la tarde si te parece. El diagnóstico tarda unos 30 minutos y si hay que formatear te aviso antes.',
 NOW() - INTERVAL '3 days' + INTERVAL '30 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'jorge_tech'),
 (SELECT id FROM users WHERE username = 'andres_cliente')),

('Perfecto, mañana en la tarde me queda bien. Te mando la dirección.',
 NOW() - INTERVAL '3 days' + INTERVAL '35 minutes',  TRUE,
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'jorge_tech')),

-- andres → ana: consulta catering (mensaje reciente no leído)
('Ana, ¿haces catering para 15 personas? Es para un cumpleaños el próximo fin de semana.',
 NOW() - INTERVAL '2 hours',  FALSE,
 (SELECT id FROM users WHERE username = 'andres_cliente'),
 (SELECT id FROM users WHERE username = 'ana_cocina')),

('¡Hola! Claro que sí, 15 personas es perfecto. ¿Tienes algún menú en mente o prefieres que te sugiera opciones?',
 NOW() - INTERVAL '1 hour 45 minutes',  FALSE,
 (SELECT id FROM users WHERE username = 'ana_cocina'),
 (SELECT id FROM users WHERE username = 'andres_cliente'));
