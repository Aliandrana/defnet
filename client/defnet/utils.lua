-- Put functions in this file to use them in several other scripts.
-- To get access to the functions, you need to put:
-- require "defnet.utils"
-- in any script using the functions.

hash_tostring = function(hash)
  return type(hash) == "string" and hash or string.match(tostring(hash), '%[([^)]+)%]')
end

url_tostring = function(url)
  if type(url) == "string" and is_system_addressee(url) then
    return url 
  end
  return type(url) == "string" and url_tostring(msg.url(url)) or string.match(tostring(url), '%[([^)]+)%]')
end

is_system_addressee = function(url)
  if not url then
    return false
  end
  return string.sub(type(url) == "string" and url or url_tostring(url), 1, 1) == "@"
end

local function deserialize_internal(elmt)
  return elmt
end

local function deserialize_internal2(elmt)
  if elmt == nil then
    return nil
  end

  local type = elmt["type"]
  local value = elmt["value"]
  if type == "table" then
    local result = {}
    for key, keyValue in pairs(value) do
      result[key] = deserialize_internal(keyValue)
    end
    return result
  elseif type == "string" then
    return value
  elseif type == "number" then
    return tonumber(value)
  elseif type == "boolean" then
    return value
  end
end

deserialize_table = function(string)
  return deserialize_internal(json.decode(string))
end

local function serialize_internal(elmt)
  if elmt == nil then
    return "null"
  end
  local type = type(elmt)
  if type == "table" then
    local result = "{"
    for key, value in pairs(elmt) do
      result = result..key..":"..serialize_internal(value)
      if next(elmt,key) ~= nil then
        result = result..", "
      end
    end
    return result.."}"
  elseif type == "string" then
    return "'"..elmt.."'"
  elseif type == "number" then
    return elmt
  elseif type == "boolean" then
    return tostring(elmt)
  else
    print("Unknow type "..type)
  end
end

local function serialize_internal2(elmt)
  if elmt == nil then
    return "null"
  end
  local type = type(elmt)
  local result = "type:'"..type.."', value:"
  if type == "table" then
    result = result.."{"
    for key, value in pairs(elmt) do
      result = result..key..":{"..serialize_internal(value).."}"
      if next(elmt,key) ~= nil then
        result = result..", "
      end
    end
    return result.."}"
  elseif type == "string" then
    return result.."'"..elmt.."'"
  elseif type == "number" then
    return result..elmt
  elseif type == "boolean" then
    return result..tostring(elmt)
  else
    print("Unknow type "..type)
  end
end

serialize_table = function(elmt)
  return ""..serialize_internal(elmt).."\n" -- { } 
end


