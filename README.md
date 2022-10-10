# paw-proxy

Proxy for at arbeidssokerregistrering-tjenester i gcp kan kalle tjenester i fss.
Tjenesten krever Azure-tokens og veksler til hva den bakomforliggende tjenesten trenger.

## Tjenester dekker
* GET /veilarbregistrering
* POST /veilarbregistrering

### Bruk
paw-proxy har en route per proxyet tjeneste, som beskrevet over. Alt etter dette i url'en blir proxyet videre.   
For eksempel vil et kall mot
`<paw-proxy>/veilarbregistrering/api/startregistrering` bli proxyet til `<veilarbregistrering>/api/startregistrering`

### Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #område-arbeid-paw
