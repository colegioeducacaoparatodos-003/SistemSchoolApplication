############################################################
# APPLICATION
############################################################
spring.config.import=optional:file:./.env[.properties]
spring.application.name=SistemSchool
server.port=8080
server.servlet.context-path=/
spring.main.allow-bean-definition-overriding=true

# IMPORTANTE: desativa lazy-init para o JSF inicializar corretamente
spring.main.lazy-initialization=false

############################################################
# SERVLET CONTEXT PARAMETERS (JSF / PrimeFaces)
# Estas propriedades são injetadas como init-params do contexto
############################################################
server.servlet.context-parameters.jakarta.faces.STATE_SAVING_METHOD=server
server.servlet.context-parameters.jakarta.faces.PARTIAL_STATE_SAVING=true
server.servlet.context-parameters.jakarta.faces.ENABLE_CDI_RESOLVER=true
server.servlet.context-parameters.jakarta.faces.PROJECT_STAGE=Development
server.servlet.context-parameters.jakarta.faces.FACELETS_SKIP_COMMENTS=true
# PrimeFaces 15 Jakarta deve usar o uploader nativo do Servlet 3+.
# O uploader Commons 1.x nao e compativel com este stack Jakarta.
server.servlet.context-parameters.primefaces.UPLOADER=native
server.servlet.context-parameters.primefaces.UPLOAD_MAX_FILE_SIZE=10485760
server.servlet.context-parameters.primefaces.SUBMIT=partial
server.servlet.context-parameters.primefaces.PRIVATE_CAPTCHA_KEY=false
server.servlet.context-parameters.primefaces.UPLOAD_MAX_FILES=10
server.servlet.context-parameters.primefaces.THEME=saga
server.servlet.context-parameters.primefaces.FONT_AWESOME=true

############################################################
# NÃO USE spring.mvc.view.* com JSF/PrimeFaces!
# O JSF usa o FacesServlet, não o Spring MVC ViewResolver
############################################################
# REMOVIDO: spring.mvc.view.prefix=/WEB-INF/views/
# REMOVIDO: spring.mvc.view.suffix=.xhtml

############################################################
# MULTIPART (upload de arquivos)
# DESATIVADO DE PROPÓSITO: com primefaces.UPLOADER=commons, é o
# PrimeFaces (via Commons FileUpload) quem deve ler o corpo multipart.
# Se o multipart do Spring ficar ativo, o DispatcherServlet consome o
# InputStream do request ANTES do filtro do PrimeFaces, e o upload
# falha silenciosamente (sem exceção, sem log, sem INSERT).
############################################################
spring.servlet.multipart.enabled=false
app.uploads.teacher-photos-dir=${user.dir}/src/main/webapp/teacher_img

############################################################
# DATABASE
############################################################
spring.datasource.url=jdbc:mysql://localhost:3306/defaultdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password= @Osael_46
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false

############################################################
# JPA / HIBERNATE
############################################################
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

############################################################
# SECURITY / SESSION
############################################################
server.servlet.session.timeout=59m

############################################################
# EMAIL
############################################################
spring.mail.host=smtp.gmail.com
spring.mail.port=587
# Gmail requires an app password when 2-Step Verification is enabled.
# Set MAIL_USERNAME and MAIL_PASSWORD in the runtime environment.
spring.mail.username=${MAIL_USERNAME:colegioeducacaoparatodos@gmail.com}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000
app.url=http://localhost:8080

############################################################
# LOGGING
############################################################
logging.level.org.springframework=INFO
spring.output.ansi.enabled=ALWAYS
app.documents.storage-path=${user.dir}/src/main/webapp/documents_files

############################################################
# ACTUATOR
############################################################
management.endpoints.web.exposure.include=startup