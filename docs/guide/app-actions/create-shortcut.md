# Create Shortcut

Adds a launcher shortcut to the generated app on your home screen. Tap ⋮ on an app card, then **Create Shortcut**.

## How it works

- WebToApp requests the launcher to pin a shortcut for the app.
- On success, a confirmation appears and the shortcut is on your home screen.

## Possible outcomes

| Result | Meaning |
| --- | --- |
| **Success** | Shortcut created. |
| **Pending** | The launcher needs to confirm; follow the prompt. |
| **Permission required** | The launcher needs the "install shortcut" permission — grant it and retry. |
| **Error** | Creation failed; the message explains why. |

## Notes

The shortcut launches the generated app directly, independent of the builder.
