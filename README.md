# capacitor-simple-social-auth4app

SimpleSocialAuth4App is a Capacitor plugin designed to simplify the process of authenticating users through various social networks using the auth4app service. It provides a seamless and secure authentication experience by opening a dedicated WebView for user credentials entry, handling callbacks, and securely returning authentication results to the Capacitor application.

## Install

```bash
npm install capacitor-simple-social-auth4app
npx cap sync
```

## API

<docgen-index>

* [`auth(...)`](#auth)
* [`addListener('authSuccess', ...)`](#addlistenerauthsuccess-)
* [`addListener('authError', ...)`](#addlistenerautherror-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### auth(...)

```typescript
auth(options: { social: SocialAuthEnum; }) => Promise<{ key: string; success: boolean; }>
```

| Param         | Type                                                                   |
| ------------- | ---------------------------------------------------------------------- |
| **`options`** | <code>{ social: <a href="#socialauthenum">SocialAuthEnum</a>; }</code> |

**Returns:** <code>Promise&lt;{ key: string; success: boolean; }&gt;</code>

--------------------


### addListener('authSuccess', ...)

```typescript
addListener(eventName: 'authSuccess', listenerFunc: (event: AuthEvent) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                                |
| ------------------ | ------------------------------------------------------------------- |
| **`eventName`**    | <code>'authSuccess'</code>                                          |
| **`listenerFunc`** | <code>(event: <a href="#authevent">AuthEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('authError', ...)

```typescript
addListener(eventName: 'authError', listenerFunc: (event: AuthEvent) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                                |
| ------------------ | ------------------------------------------------------------------- |
| **`eventName`**    | <code>'authError'</code>                                            |
| **`listenerFunc`** | <code>(event: <a href="#authevent">AuthEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### AuthEvent

| Prop           | Type                |
| -------------- | ------------------- |
| **`key`**      | <code>string</code> |
| **`userInfo`** | <code>any</code>    |


### Enums


#### SocialAuthEnum

| Members      | Value                 |
| ------------ | --------------------- |
| **`VK`**     | <code>'vk'</code>     |
| **`OK`**     | <code>'ok'</code>     |
| **`GOOGLE`** | <code>'google'</code> |
| **`YANDEX`** | <code>'yandex'</code> |
| **`MAILRU`** | <code>'mailru'</code> |

</docgen-api>
