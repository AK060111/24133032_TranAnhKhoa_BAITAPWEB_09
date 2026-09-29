SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_products PRIMARY KEY,
        name NVARCHAR(500) NOT NULL,
        description NVARCHAR(2000) NULL,
        price DECIMAL(18,2) NOT NULL,
        image_url VARCHAR(1500) NULL,
        image_public_id VARCHAR(500) NULL,
        user_id BIGINT NOT NULL,
        created_at DATETIME2(6) NOT NULL CONSTRAINT df_products_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT ck_products_price_nonnegative CHECK (price >= 0),
        CONSTRAINT fk_products_users FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.products') AND name = N'idx_products_name')
    CREATE INDEX idx_products_name ON dbo.products(name);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.products') AND name = N'idx_products_user_id')
    CREATE INDEX idx_products_user_id ON dbo.products(user_id);

IF OBJECT_ID(N'dbo.otp_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.otp_tokens (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_otp_tokens PRIMARY KEY,
        email VARCHAR(120) NOT NULL,
        otp_hash VARCHAR(100) NOT NULL,
        type VARCHAR(30) NOT NULL,
        expires_at DATETIME2(6) NOT NULL,
        attempts INT NOT NULL,
        used BIT NOT NULL CONSTRAINT df_otp_tokens_used DEFAULT 0,
        created_at DATETIME2(6) NOT NULL,
        CONSTRAINT ck_otp_tokens_attempts_nonnegative CHECK (attempts >= 0)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.otp_tokens') AND name = N'idx_otp_email_type')
    CREATE INDEX idx_otp_email_type ON dbo.otp_tokens(email, type);

COMMIT TRANSACTION;
