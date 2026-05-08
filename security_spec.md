# Security Specification for Eyris

## Data Invariants
1. **User Profiles (`/users/{userId}`)**:
   - Only the authenticated user whose `uid` matches the `{userId}` can read or write their own profile.
   - Profile documents must contain valid `userId`, `displayName`, `email`, `lastOnline`, and `createdAt` fields.
   - `userId` and `createdAt` are immutable after creation.

2. **Leads (`/leads/{leadId}`)**:
   - Only the authenticated user who created the lead (`userId` field) can read, update, or delete it.
   - Leads must belong to a valid user.
   - `userId` is immutable after creation.

3. **Contacted Leads (`/contacted/{contactedId}`)**:
   - Only the authenticated user who created the record (`userId` field) can read, update, or delete it.
   - Status must be one of: `ANSWERED`, `ACCEPTED`, `REJECTED`, `GHOSTED`.
   - `userId` is immutable after creation.

## The Dirty Dozen Payloads (Expected to be DENIED)
1. Write to `/users/another_user`: `{"userId": "another_user", ...}`
2. Read from `/users/another_user`.
3. Update `userId` in `/users/{myId}` to `new_id`.
4. Create Lead in `/leads/new_lead` with `userId: "not_me"`.
5. Read Lead from `/leads/some_lead` where `userId != my_uid`.
6. Update `userId` of an existing Lead.
7. Delete Lead belonging to another user.
8. Create ContactedLead with `status: "INVALID_STATUS"`.
9. Create Lead with `businessName` exceeding 500 characters.
10. Update `createdAt` in a User profile.
11. Write to random path `/unprotected/data`.
12. List leads without filtering by `userId`.

## Red Team Evaluation Logic
- All writes must be validated by `isValid[Entity]()` helpers.
- All updates must use `affectedKeys().hasOnly()` gates to prevent shadow fields.
- Identity is verified via `request.auth.uid`.
- PII (email) is restricted to the owner.
