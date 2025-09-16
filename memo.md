
$env:HMAC_SECRET="devsecret"
$env:DISCORD_WEBHOOK_URL="https://discord.com/api/webhooks/1417183835241713704/ko1481-q46Ydz_6LY7i1Qw63A0Vaq3UqPU1kUXgz2ciIh4_ColRnUtwBd9h10iY4hlQ0"


-テスト用コマンド（bashで実行）
 curl -X POST   -H "Content-Type: application/json"   -d '{"content": "Hello, Discord!:tada:"}' https://discord.com/api/webhooks/1417183835241713704/ko1481-q46Ydz_6LY7i1Qw63A0Vaq3UqPU1kUXgz2ciIh4_ColRnUtwBd9h10iY4hlQ0

 