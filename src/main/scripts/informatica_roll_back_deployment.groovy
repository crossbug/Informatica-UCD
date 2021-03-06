final def out = System.out
final def LS = System.getProperty("line.separator")
final def unique = System.currentTimeMillis()

final def stepProps = new Properties();
final def inputPropsFile = new File(args[0]);
final def inputPropsStream = null;
try {
    inputPropsStream = new FileInputStream(inputPropsFile);
    stepProps.load(inputPropsStream);
}
catch (IOException e) {
    throw new RuntimeException(e);
}

final def groupname = stepProps['groupname']
final def numrevs   = stepProps['numrevs']
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def host      = stepProps['host']
final def port      = stepProps['port']

final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'

def script = new File(inputFile)
script.deleteOnExit()
script << "connect -r $repo -n $username -x $password "
if (domain) {
    script << "-d $domain $LS"
}
else {
    script << "-h $host -o $port $LS"
}
script << "rollbackdeployment -p $groupname -n $numrevs $LS"
script << "exit"

println('script content:')
script.eachLine { line -> println(line) }
println('')

def command = []
command.add('pmrep')
command.add('run')
command.add('-o')       
command.add(outputFile)
command.add('-f')
command.add(inputFile)
command.add('-e')
command.add('-s')

println('command:')
println(command.join(' '))
println('')

def process = command.execute()
process.consumeProcessOutput(out, out)
process.waitFor()

def output = new File(outputFile)
Scanner sc = new Scanner(output)
println('pmrep output:')
def lastLine = ""
while (sc.hasNextLine()) {
    lastLine = sc.nextLine()
    println(lastLine)
}
println('')
sc.close()

script.delete()
output.delete()


if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
    System.exit(1)
}
else {
    System.exit(process.exitValue())
}
