> [!WARNING]
> Appen er ikke lengre i bruk og koden er arkivert

# paw-proxy

Proxy for at arbeidssokerregistrering-tjenester i gcp kan kalle tjenester i fss.
Tjenesten krever Azure-tokens og veksler til hva den bakomforliggende tjenesten trenger.

### Bruk
paw-proxy har en route per proxyet tjeneste, som beskrevet over. Alt etter dette i url'en blir proxyet videre.   
For eksempel vil et kall mot
`<paw-proxy>/veilarboppfolging/api/test` bli proxyet til `<veilarboppfolging>/api/test`

### Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #område-arbeid-paw
