require "defnet.utils"

defnet = {
  post = function(urlTo, nameHash, paramsObj)
    paramsObj = paramsObj or {}
    local fromUrl = msg.url(".")
    local toString = url_tostring(urlTo) 
    local nameString = hash_tostring(nameHash)
    if not is_system_addressee(to) then --if not system call    
      msg.post(to, nameHash, paramsObj)
    end
    msg.post("default:/defnet#script", "send_message", {from = fromUrl, to = toString, name = nameString, params = paramsObj})
  end
, spawn = function(url, position, rotation, properties, scale)
    local id = factory.create(url, position, rotation, properties or {}, scale)
    if id == nil then
      return
    end
    msg.post(msg.url(id), "init_local")   
    msg.post("default:/defnet#script", "send_spawn", {obj = id, factory = url_tostring(url), position = position, rotation = rotation, properties = properties, scale = scale})
  end
, watch = function()
    msg.post("default:/defnet#script", "watch")    
  end
, connect = function(hostString, port, appId)
    msg.post("default:/defnet#script", "connect", {host = hostString, port = port, appId = appId})    
  end
}