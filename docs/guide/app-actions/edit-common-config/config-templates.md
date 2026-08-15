# Config Templates

Save the whole common config as a named template once, then apply it to any app in one tap — so a heavily customized browser setup never has to be re-configured app by app.

**Where:** the **Config Templates** card at the top of the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## What a template contains

A template is a **full snapshot of the common config (`WebViewConfig`)** — browser behavior, interface, network, privacy, toolbar and everything else configured in that editor. Applying a template **replaces** the target app's entire common config with the snapshot: the result is exactly what was saved, never a mix of old and new values. App-type-specific settings (entry files, ports, dependencies) and APK export settings are not part of a template.

## Using templates

- **Save as template** — snapshots the config currently shown in the editor under a name (1–40 chars). Saving again with the same name overwrites the old snapshot.
- **Apply** — tap a template name in the card to replace the current app's common config with it. The change lands in the editor like any manual edit; review and save as usual.
- **Manage templates** — rename or delete saved templates.

## Notes

- Templates are stored locally (`config_templates.json` in app storage) and survive app updates.
- The in-app Agent can also manage templates: `ListConfigTemplates`, `SaveConfigTemplate`, `ApplyConfigTemplate`, `DeleteConfigTemplate`.
