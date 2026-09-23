# Legacy photo deletion stopgap

## Scope

RateController, PreciousController and CartoonController previously constructed a
directory from the shared photo root plus a record's certificate number and
recursively deleted it. An empty certificate number selects the shared root.
The incident trigger still requires confirmation from production logs.

## Policy

Record deletion, range deletion, renumbering and photo replacement no longer
physically remove existing photos through deleteResource/deleteOldResource.
Those two private methods deliberately retain files for all inputs, including
valid identifiers. Database record operations are otherwise unchanged. This
trades disk space for recoverability and avoids path, symlink and shared-file
ownership hazards in these cleanup paths. Do not reintroduce recursive cleanup
without a separate reviewed retention/recovery workflow.

This is not a claim that every file operation in the legacy application has
been hardened: temporary upload moves and generated export cleanup are outside
this stopgap. Existing missing photos are not recreated by this code change.

## Verification and release boundary

LegacyPhotoRetentionTest covers all three controllers with disposable local
fixtures: blank/null identifiers, traversal/absolute/Windows-like paths,
renumbering/deleting valid records, and malformed photo replacement inputs.
It checks root, selected record, sibling record and outside sentinel contents.

Local source fix only. No production database/file changes or restart performed.
Production remains vulnerable until a reviewed hotfix is explicitly deployed.
Do not bundle the unfinished temporary-import release into the emergency fix.
Before rollout preserve the current disk state/logs and verify the original
production JAR, Java executable and startup arguments. Never roll back an entire
disk or database as part of deploying this code fix.
