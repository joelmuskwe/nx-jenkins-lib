import groovy.json.JsonSlurperClassic 

def createBranches(res) {
  def branches = [:]
  res.commands.each {c->
    branches[c.name] = {
      node {
        sh c.command
        c.outputs.each { o->
          stash name: o.name, includes: o.files 
        }
      }
    }
  }
  return branches
}

def call(command) {
  
String s = """
{
  "commands": [
    {
      "name": "reactapp-build",
      "command": "yarn nx build reactapp",
      "outputs": [
        {"name": "reactapp-build-output",  "files": "dist/apps/reactapp/**"}
      ]
    },
    {
      "name": "angularapp-build",
      "command": "yarn nx build angularapp",
      "outputs": [
        {"name": "angularapp-build-output",  "files": "dist/apps/angularapp/**"}
      ]
    }
  ]
}
"""
  final res = new groovy.json.JsonSlurperClassic().parseText(s)
  parallel createBranches(res)
  res.each {c->
    c.outputs.each {o->
      unstach name: o.name
    }
  }
}