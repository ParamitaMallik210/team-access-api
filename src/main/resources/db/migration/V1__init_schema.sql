-- Flyway runs this file ONCE, in order, when the app starts.
-- Filename rules: V<number>__<description>.sql  (two underscores)

-- Who can log in. One person, many organizations (via memberships).
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- A tenant: a company/team that owns members, invites, audit logs.
CREATE TABLE organizations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Catalog of actions, e.g. members:invite, billing:manage (seeded on Day 4).
CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Named bundles of permissions. is_system = global Owner/Admin/Member/Viewer.
-- organization_id NULL means a system role shared by all orgs.
CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    is_system       BOOLEAN NOT NULL DEFAULT FALSE,
    organization_id UUID REFERENCES organizations (id)
);

CREATE UNIQUE INDEX uq_roles_system_name
    ON roles (name)
    WHERE organization_id IS NULL;

CREATE UNIQUE INDEX uq_roles_org_name
    ON roles (organization_id, name)
    WHERE organization_id IS NOT NULL;

-- Which permissions a role has. No extra columns, so this is a pure join table.
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- The heart of multi-tenancy: this user, in this org, has this role.
-- UNIQUE means one membership per user per org.
CREATE TABLE memberships (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES roles (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, organization_id)
);

-- Login sessions / refresh tokens (hashed). Day 7 may also use Redis.
CREATE TABLE sessions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at         TIMESTAMPTZ NOT NULL,
    revoked_at         TIMESTAMPTZ,
    ip_address         VARCHAR(45),
    user_agent         VARCHAR(512),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Pending teammate invites. token_hash so a leaked DB does not reveal the raw link.
CREATE TABLE invitations (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id    UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email              VARCHAR(255) NOT NULL,
    role_id            UUID NOT NULL REFERENCES roles (id),
    token_hash         VARCHAR(255) NOT NULL UNIQUE,
    invited_by_user_id UUID NOT NULL REFERENCES users (id),
    expires_at         TIMESTAMPTZ NOT NULL,
    accepted_at        TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Append-only history of sensitive actions. We do not update these rows.
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL,
    actor_user_id   UUID REFERENCES users (id) ON DELETE SET NULL,
    action          VARCHAR(100) NOT NULL,
    target_type     VARCHAR(100),
    target_id       UUID,
    ip_address      VARCHAR(45),
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_memberships_org ON memberships (organization_id);
CREATE INDEX idx_sessions_user ON sessions (user_id);
CREATE INDEX idx_invitations_org ON invitations (organization_id);
CREATE INDEX idx_audit_logs_org_created ON audit_logs (organization_id, created_at DESC);
